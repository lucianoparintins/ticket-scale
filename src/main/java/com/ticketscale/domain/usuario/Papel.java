package com.ticketscale.domain.usuario;

public enum Papel {
    ADMIN("admin"),
    USUARIO("usuario");

    private String papel;

    Papel(String papel) {
        this.papel = papel;
    }

    public String getPapel() {
        return papel;
    }
}
