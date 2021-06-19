package br.com.zup.luiz.interceptor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.IllegalArgumentException

internal class ExceptionHandlerResolverTest{

    lateinit var exceptionHandler: ExceptionHandler<Exception>

    lateinit var resolver: ExceptionHandlerResolver

    @BeforeEach
    internal fun setUp() {
        exceptionHandler = object : ExceptionHandler<java.lang.Exception>{
            override fun handle(e: java.lang.Exception): ExceptionHandler.StatusWithDetails {
                TODO("Not yet implemented")
            }

            override fun supports(e: Exception) = e is java.lang.IllegalArgumentException
        }

        resolver = ExceptionHandlerResolver(handlers = listOf(exceptionHandler))
    }

    @Test
    fun `deve retornar o handler especifico para o tipo de excecao`() {
        val resolved = resolver.resolve(IllegalArgumentException())

        assertSame(exceptionHandler, resolved)
    }

    @Test
    fun `deve retornar o handler padrao quando nenhum handler suportar o tipo da excecao`() {
        val resolved = resolver.resolve(RuntimeException())

        assertTrue(resolved is DefaultExceptionHandler)
    }

    @Test
    fun `deve lancar um erro se encontrar mais de um ExceptionHandler que suporta a mesma excecao`() {
        resolver = ExceptionHandlerResolver(listOf(exceptionHandler, exceptionHandler))

        assertThrows<IllegalStateException> { resolver.resolve(IllegalArgumentException()) }
    }
}