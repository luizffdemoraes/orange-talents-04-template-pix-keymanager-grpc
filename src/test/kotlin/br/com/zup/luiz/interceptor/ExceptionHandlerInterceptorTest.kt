package br.com.zup.luiz.interceptor


import org.junit.jupiter.api.Assertions.*
import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.RuntimeException


@ExtendWith(MockitoExtension::class)
internal class ExceptionHandlerInterceptorTest{
    @Mock
    lateinit var context: MethodInvocationContext<BindableService, Any?>

    val interceptor = ExceptionHandlerInterceptor(resolver = ExceptionHandlerResolver(handlers = emptyList()))

    @Test
    fun `deve capturar a excecao e gerar um erro com resposta gRPC`(@Mock streamObserver: StreamObserver<*>){
        with(context){
            `when`(proceed())
                .thenThrow(RuntimeException("Uma exceção ocorreu"))

            `when`(parameterValues)
                .thenReturn(arrayOf(null, streamObserver))
        }

        interceptor.intercept(context).run {
            Mockito.verify(streamObserver).onError(Mockito.notNull())
        }
    }

    @Test
    fun `se o metodo nao gerar nenhuma excecao, deve apenas retornar a mesma resposta`() {
        val expected = "whatever"

        `when`(context.proceed()).thenReturn(expected)

        assertEquals(expected, interceptor.intercept(context))
    }

}
