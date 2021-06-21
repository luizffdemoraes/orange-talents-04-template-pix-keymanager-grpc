package br.com.zup.luiz.remove

import br.com.zup.luiz.exceptions.ChavePixNaoEncontradaException
import br.com.zup.luiz.integration.bcb.BancoCentralClient
import br.com.zup.luiz.integration.bcb.DeletePixKeyRequest
import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.pix.ContaAssociada
import br.com.zup.luiz.validations.ValidUUID
import io.micronaut.data.annotation.event.PostRemove
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank


@Validated
@Singleton
class RemoveChaveService(@Inject val repository: ChavePixRepository,
                         @Inject val bcbClient: BancoCentralClient) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID clienteId: String?, // 1
        @NotBlank @ValidUUID pixId: String?,
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow{ ChavePixNaoEncontradaException("Chave pix não encontrada ou não pertence ao usuário") }

        repository.delete(chave)

        val request = DeletePixKeyRequest(chave.chave) // 1

        val bcbResponse = bcbClient.delete(key = chave.chave, request = request) // 1
        if (bcbResponse.status != HttpStatus.OK) { // 1
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
        }
    }
}