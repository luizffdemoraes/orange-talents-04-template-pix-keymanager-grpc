package br.com.zup.luiz.carrega

import br.com.zup.luiz.CarregaChavePixResponse
import br.com.zup.luiz.TipoDeChave
import br.com.zup.luiz.TipoDeConta
import br.com.zup.luiz.pix.ChavePixInfo

import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): CarregaChavePixResponse {
        return CarregaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChave(CarregaChavePixResponse.ChavePix // 1
                .newBuilder()
                .setTipo(TipoDeChave.valueOf(chaveInfo.tipo.name)) // 2
                .setChave(chaveInfo.chave)
                .setConta(CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder() // 1
                    .setTipo(TipoDeConta.valueOf(chaveInfo.tipoDeConta.name)) // 2
                    .setInstituicao(chaveInfo.conta.instituicao) // 1 (Conta)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }
}