package br.com.zup.luiz.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveTest {

    @Nested
    inner class Aleatoria {

        @Test
        fun `deve ser valido quando chave aleatoria for ula ou vazia`() {
            with(TipoDeChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave aleatoria possuir um valor`() {
            with(TipoDeChave.ALEATORIA) {
                assertFalse(valida("um valor qualquer"))
            }
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando cpf for um numero valido`() {
            with(TipoDeChave.CPF) {
                assertTrue(valida("35060731332"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for um numero invalido`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida("3506071331"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf nao for informado`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf possuir letras`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida("3506073133a"))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando celular for um numero valido`() {
            with(TipoDeChave.CELULAR) {
                assertTrue(valida("+5511987932055"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero invalido`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida("11987654321"))
                assertFalse(valida("+55a11987932055"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero nao for valido`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }

        @Nested
        inner class EMAIL {
            @Test
            fun `deve ser valido quando email for endereco valido`() {
                with(TipoDeChave.EMAIL) {
                    assertTrue(valida("teste@zup.com.br"))
                }
            }

            @Test
            fun `nao deve ser valido quando email estiver em um formato invalido`() {
                with(TipoDeChave.EMAIL) {
                    assertFalse(valida("testezup.com.br"))
                    assertFalse(valida("testezup.com."))
                }
            }

            @Test
            fun `nao deve ser valido quando email nao for informado`() {
                with(TipoDeChave.EMAIL) {
                    assertFalse(valida(null))
                    assertFalse(valida(""))
                }
            }

        }

    }


}