package br.com.zup.luiz.registra

import br.com.zup.luiz.exceptions.ChavePixExistenteException
import br.com.zup.luiz.integration.bcb.BancoCentralClient
import br.com.zup.luiz.integration.bcb.CreatePixKeyRequest
import br.com.zup.luiz.integration.itau.ItauClient
import br.com.zup.luiz.pix.ChavePix
import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.pix.NovaChavePix
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated // essa classe será validada de acordo com as anotacoes do dto
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BancoCentralClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional //transactional pois salvará nossa chave pix no banco
    //retorna nossa entidade modelo de chave pix
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        // 1. Verifica se chave já existe no sistema, se ja existe, throws exception
        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        // 2. Busca dados da conta no ERP do ITAU - se não retornar um body, requisicao falhou, throws exception
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        // 3. Grava no banco de dados
        val chave = novaChave.toModel(conta)
        repository.save(chave)

        // 4. Registra chave no BCB, porém antes convertemos nossa chave para o modelo de chave esperado pelo BCB
        val bcbRequest = CreatePixKeyRequest.of(chave).also {
            LOGGER.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
        }


        // -> 201 created (sucesso) ou 422 unprocessable entity (falha)
        val bcbResponse = bcbClient.create(bcbRequest)
        if (bcbResponse.status != HttpStatus.CREATED) {
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
        }

        // 5. Atualiza chave do dominio com chave gerada pelo BCB
        chave.atualiza(bcbResponse.body()!!.key)

        return chave
    }
}