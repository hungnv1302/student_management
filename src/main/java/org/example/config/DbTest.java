package org.example.config;

import java.sql.Connection;

public class DbTest {
    public static void main(String[] args) throws Exception {
        try (Connection c = DbConfig.getInstance().getConnection()) {
            System.out.println("✅ Connected: " + (c != null && !c.isClosed()));
            System.out.println("DB: " + c.getCatalog());
        }
    }
}
