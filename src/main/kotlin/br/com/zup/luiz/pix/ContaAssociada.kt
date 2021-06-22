package br.com.zup.luiz.pix

import javax.persistence.*
import javax.validation.constraints.*

@Embeddable
class ContaAssociada(
    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeDoTitular: String,

    @field:NotBlank
    @field:Size(max = 11)
    val cpfDoTitular: String,

    @field:NotBlank
    @field:Size(max = 4)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    val numeroDaConta: String
) {

    companion object {
        val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}