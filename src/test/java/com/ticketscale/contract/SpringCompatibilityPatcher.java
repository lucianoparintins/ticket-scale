package com.ticketscale.contract;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Patcher de compatibilidade em tempo de execução para RestAssured MockMvc com Spring Framework 7 / Spring Boot 4.
 * Redireciona chamadas virtuais de métodos covariantes em MockHttpServletRequestBuilder para a superclasse
 * AbstractMockHttpServletRequestBuilder com checagem de tipo (CHECKCAST).
 */
public final class SpringCompatibilityPatcher {

    private static final String BUILDER_CLASS =
            "org/springframework/test/web/servlet/request/MockHttpServletRequestBuilder";
    private static final String ABSTRACT_BUILDER_CLASS =
            "org/springframework/test/web/servlet/request/AbstractMockHttpServletRequestBuilder";
    private static final String BUILDER_DESC_SUFFIX =
            ")Lorg/springframework/test/web/servlet/request/MockHttpServletRequestBuilder;";
    private static final String ABSTRACT_BUILDER_DESC_SUFFIX =
            ")Lorg/springframework/test/web/servlet/request/AbstractMockHttpServletRequestBuilder;";

    private static volatile boolean patched = false;

    private SpringCompatibilityPatcher() {
    }

    @SuppressWarnings("IllegalCatch")
    public static synchronized void apply() {
        if (patched) {
            return;
        }
        try {
            ByteBuddyAgent.install();
            new AgentBuilder.Default()
                    .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                    .type(ElementMatchers.nameStartsWith("io.restassured.module.mockmvc.internal."))
                    .transform((builder, typeDesc, classLoader, module, protectionDomain) ->
                            builder.visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                    .invokable(ElementMatchers.any(),
                                            (targetDesc, methodDesc, methodVisitor, implContext,
                                             typePool, writerFlags, readerFlags) ->
                                                    new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                                                        @Override
                                                        public void visitMethodInsn(
                                                                int opcode,
                                                                String owner,
                                                                String name,
                                                                String descriptor,
                                                                boolean isInterface) {
                                                            if (opcode == Opcodes.INVOKEVIRTUAL
                                                                    && BUILDER_CLASS.equals(owner)
                                                                    && descriptor.endsWith(BUILDER_DESC_SUFFIX)) {
                                                                int lastParen = descriptor.lastIndexOf(')');
                                                                String newDesc = descriptor.substring(0, lastParen)
                                                                        + ABSTRACT_BUILDER_DESC_SUFFIX;
                                                                super.visitMethodInsn(
                                                                        opcode,
                                                                        ABSTRACT_BUILDER_CLASS,
                                                                        name,
                                                                        newDesc,
                                                                        isInterface);
                                                                super.visitTypeInsn(
                                                                        Opcodes.CHECKCAST,
                                                                        BUILDER_CLASS);
                                                            } else {
                                                                super.visitMethodInsn(
                                                                        opcode,
                                                                        owner,
                                                                        name,
                                                                        descriptor,
                                                                        isInterface);
                                                            }
                                                        }
                                                    }
                                    )
                            )
                    )
                    .installOn(ByteBuddyAgent.getInstrumentation());
            patched = true;
        } catch (IllegalStateException | SecurityException | IllegalArgumentException e) {
            throw new RuntimeException("Falha ao aplicar patch de compatibilidade para RestAssured MockMvc", e);
        }
    }
}
