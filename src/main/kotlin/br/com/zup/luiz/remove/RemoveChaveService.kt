package br.com.zup.luiz.remove

import br.com.zup.luiz.exceptions.ChavePixNaoEncontradaException
import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.validations.ValidUUID
import io.micronaut.data.annotation.event.PostRemove
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank


@Validated
@Singleton
class RemoveChaveService(@Inject val repository: ChavePixRepository) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID(message = "Cliente ID com formato inválido") clienteId: String?,
        @NotBlank @ValidUUID(message = "pix ID com formato inválido") pixId: String?
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow{ ChavePixNaoEncontradaException("Chave pix não encontrada ou não pertence ao usuário") }

        repository.delete(chave)
    }
}