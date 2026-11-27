import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    // Frontend de BiblioTech (ng serve o docker compose) y API del backend
    baseUrl: 'http://localhost:4200',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    supportFile: 'cypress/support/e2e.ts',
    video: false,
    expose: {
      apiUrl: 'http://localhost:8080/api'
    }
  }
});
