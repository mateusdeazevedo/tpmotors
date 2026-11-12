/**
 * TPMotors - API Client & Gerenciamento de Estado JWT (Puro JS/Fetch)
 */

const API_BASE_URL = 'http://localhost:8080/api';

const Auth = {
  getToken() {
    return localStorage.getItem('tpmotors_jwt');
  },

  setToken(token) {
    localStorage.setItem('tpmotors_jwt', token);
  },

  getUserData() {
    const raw = localStorage.getItem('tpmotors_user');
    return raw ? JSON.parse(raw) : null;
  },

  setUserData(data) {
    localStorage.setItem('tpmotors_user', JSON.stringify(data));
  },

  logout() {
    localStorage.removeItem('tpmotors_jwt');
    localStorage.removeItem('tpmotors_user');
    window.location.href = 'login.html';
  },

  /**
   * Decodifica o payload do JWT gerado pelo Spring Boot:
   * Payload contém: { user: "nome", status: "admin" | "user", role: "...", sub: "email" }
   */
  decodePayload(token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Falha ao decodificar token JWT:', e);
      return null;
    }
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  isAdmin() {
    const user = this.getUserData();
    return user && (user.status === 'admin' || user.role === 'ADMIN');
  },

  /**
   * Proteção de página: redireciona se não autorizado
   */
  requireAuth(requiredStatus = null) {
    if (!this.isAuthenticated()) {
      window.location.href = 'login.html';
      return false;
    }

    if (requiredStatus === 'admin' && !this.isAdmin()) {
      alert('Acesso restrito a administradores do sistema.');
      window.location.href = 'painel-usuario.html';
      return false;
    }

    return true;
  }
};

/**
 * Cliente HTTP unificado utilizando Fetch API nativa.
 * Injeta automaticamente o Bearer Token e intercepta 401 / 429 / 404.
 */
async function apiFetch(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
  
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  const token = Auth.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers
    });

    // Tratamento de Rate Limit (Mecanismo 2 de Segurança)
    if (response.status === 429) {
      const data = await response.json().catch(() => ({}));
      const msg = data.message || 'Muitas requisições. Rate Limit excedido por IP.';
      alert('⚠️ Limite de Requisições: ' + msg);
      throw new Error(msg);
    }

    // Tratamento de Token Expirado ou Não Autorizado
    if (response.status === 401) {
      Auth.logout();
      throw new Error('Sessão expirada. Faça login novamente.');
    }

    // Tratamento de Rota Não Encontrada (404)
    if (response.status === 404) {
      const data = await response.json().catch(() => ({}));
      throw new Error(data.message || 'Recurso não encontrado (HTTP 404).');
    }

    // Sem conteúdo (ex: DELETE 204)
    if (response.status === 204) {
      return null;
    }

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Erro ao processar requisição.');
    }

    return data;
  } catch (err) {
    console.error(`Erro na requisição [${endpoint}]:`, err);
    throw err;
  }
}

// Utilitários de Formatação
const Formatters = {
  moeda(valor) {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor || 0);
  },

  km(valor) {
    return new Intl.NumberFormat('pt-BR').format(valor || 0) + ' km';
  }
};
