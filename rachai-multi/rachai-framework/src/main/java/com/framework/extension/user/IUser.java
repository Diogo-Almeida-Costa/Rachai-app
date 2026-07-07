package com.framework.extension.user;


/**
 * Contrato mínimo que cada instância deve cumprir para que o framework
 * consiga operar sobre o conceito de "usuário" sem depender da entidade
 * JPA concreta de cada aplicação (cada instância tem sua própria tabela,
 * autenticação, etc).
 *
 * A instância deve fazer sua entidade real (ex: AppUser) implementar esta
 * interface, ou criar um adapter/wrapper em volta dela.
 */

public interface IUser {
    Long getId();
    String getName();
    String getEmail();
    
}


 

