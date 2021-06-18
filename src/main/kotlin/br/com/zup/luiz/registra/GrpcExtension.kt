package br.com.zup.luiz.registra

import br.com.zup.luiz.RegistraChavePixRequest
import br.com.zup.luiz.TipoDeConta
import br.com.zup.luiz.TipoDeConta.*
import br.com.zup.luiz.pix.NovaChavePix
import br.com.zup.luiz.pix.TipoDeChave
import br.com.zup.luiz.TipoDeChave.*

fun RegistraChavePixRequest.toModel() : NovaChavePix {
    return NovaChavePix( // 1
        clienteId = clienteId,
        tipoDeChave = when (tipoDeChave) {
            //registrado que esse enum é gerado pelo proto
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name) // 1
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            //registrado que esse enum é gerado pelo proto
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name) // 1
        }
    )
}