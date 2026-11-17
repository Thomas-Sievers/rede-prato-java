/* Rede de Prato — API client, formatters, illustrations and icons (no external libraries). */
(function () {
  'use strict';

  async function request(method, path, body) {
    const opts = { method, headers: { Accept: 'application/json' } };
    if (body !== undefined) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    let res;
    try {
      res = await fetch(path, opts);
    } catch (e) {
      throw { status: 0, message: 'Sem conexão com o servidor. Tente de novo.', fields: null };
    }
    if (res.status === 204) return null;
    let data = null;
    const text = await res.text();
    if (text) {
      try { data = JSON.parse(text); } catch (e) { data = null; }
    }
    if (!res.ok) {
      throw {
        status: res.status,
        message: (data && data.message) || 'Algo deu errado. Tente de novo.',
        fields: (data && data.fields) || null
      };
    }
    return data;
  }

  const api = {
    get: (p) => request('GET', p),
    post: (p, b) => request('POST', p, b === undefined ? {} : b),
    put: (p, b) => request('PUT', p, b),
    del: (p) => request('DELETE', p)
  };

  /** Builds "path?a=1&b=2", skipping empty values. */
  function qs(path, params) {
    const u = new URLSearchParams();
    Object.keys(params || {}).forEach((k) => {
      const v = params[k];
      if (v !== null && v !== undefined && v !== '') u.append(k, v);
    });
    const s = u.toString();
    return s ? path + '?' + s : path;
  }

  function esc(v) {
    return String(v === null || v === undefined ? '' : v)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  const nf = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
  const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
  const fmtKg = (v) => nf.format(Number(v || 0)) + ' kg';
  const fmtNum = (v) => nf.format(Number(v || 0));
  const fmtBRL = (v) => brl.format(Number(v || 0));

  function parseLocal(iso) {
    if (!iso) return null;
    const m = String(iso).match(/^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}))?/);
    if (!m) return null;
    return new Date(+m[1], +m[2] - 1, +m[3], +(m[4] || 0), +(m[5] || 0));
  }

  /** "18h" or "18h30". */
  function fmtHora(iso) {
    const d = parseLocal(iso);
    if (!d) return '';
    return d.getHours() + 'h' + (d.getMinutes() ? String(d.getMinutes()).padStart(2, '0') : '');
  }

  function sameDay(a, b) {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  }

  /** "hoje, 18h" / "amanhã, 10h" / "20/11, 10h". */
  function fmtDia(iso) {
    const d = parseLocal(iso);
    if (!d) return '';
    const hoje = new Date();
    const amanha = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() + 1);
    let dia;
    if (sameDay(d, hoje)) dia = 'hoje';
    else if (sameDay(d, amanha)) dia = 'amanhã';
    else dia = String(d.getDate()).padStart(2, '0') + '/' + String(d.getMonth() + 1).padStart(2, '0');
    return dia + ', ' + fmtHora(iso);
  }
  const fmtPrazo = (iso) => 'até ' + fmtDia(iso);

  /** "17/11/2026 18h". */
  function fmtData(iso) {
    const d = parseLocal(iso);
    if (!d) return '';
    return String(d.getDate()).padStart(2, '0') + '/' + String(d.getMonth() + 1).padStart(2, '0') + '/' +
      d.getFullYear() + ' ' + fmtHora(iso);
  }

  /** Local ISO without zone: "2026-11-17T18:00:00". */
  function isoLocal(d) {
    const p = (n) => String(n).padStart(2, '0');
    return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + 'T' + p(d.getHours()) + ':' +
      p(d.getMinutes()) + ':00';
  }

  // ---------- Illustrations (flat SVG, decorative) ----------
  const ILL = {
    tomate: '<svg viewBox="0 0 100 100" aria-hidden="true"><circle cx="50" cy="58" r="34" fill="#D9452B"/><ellipse cx="38" cy="48" rx="8" ry="5" fill="#F08A6E"/><path d="M50 26 l-14 -6 l10 10 l-12 2 l14 4 l2 12 l4 -12 l14 -4 l-12 -2 l10 -10 z" fill="#2F6B2B"/></svg>',
    alface: '<svg viewBox="0 0 100 100" aria-hidden="true"><circle cx="50" cy="56" r="34" fill="#6BAE45"/><circle cx="50" cy="56" r="24" fill="#9CCC6C"/><circle cx="50" cy="56" r="13" fill="#C8E6A0"/><path d="M50 30 v52 M30 46 l20 10 l20 -10" stroke="#2F6B2B" stroke-width="3" fill="none" stroke-linecap="round"/></svg>',
    cenoura: '<svg viewBox="0 0 100 100" aria-hidden="true"><path d="M42 30 L58 30 L52 92 Q50 96 48 92 Z" fill="#EE8A2B"/><path d="M44 44 h6 M46 58 h6 M47 72 h5" stroke="#B85E12" stroke-width="3" stroke-linecap="round"/><path d="M50 30 l-10 -20 M50 30 l0 -22 M50 30 l10 -20" stroke="#2F6B2B" stroke-width="6" stroke-linecap="round"/></svg>',
    banana: '<svg viewBox="0 0 100 100" aria-hidden="true"><path d="M18 30 Q30 82 84 66 Q88 62 82 60 Q38 66 30 24 Z" fill="#F2C230"/><path d="M26 26 l-4 -8" stroke="#6B4A16" stroke-width="6" stroke-linecap="round"/></svg>',
    maca: '<svg viewBox="0 0 100 100" aria-hidden="true"><path d="M50 34 C30 20 12 40 20 64 C26 84 42 92 50 84 C58 92 74 84 80 64 C88 40 70 20 50 34 Z" fill="#D92B2B"/><path d="M50 34 Q52 22 58 16" stroke="#6B4A16" stroke-width="5" fill="none" stroke-linecap="round"/><ellipse cx="64" cy="20" rx="10" ry="5" fill="#2F6B2B" transform="rotate(-25 64 20)"/></svg>',
    laranja: '<svg viewBox="0 0 100 100" aria-hidden="true"><circle cx="50" cy="56" r="34" fill="#F29A2B"/><circle cx="40" cy="46" r="3" fill="#F7BE6E"/><circle cx="58" cy="62" r="3" fill="#F7BE6E"/><circle cx="46" cy="68" r="3" fill="#F7BE6E"/><ellipse cx="58" cy="22" rx="11" ry="5" fill="#2F6B2B" transform="rotate(-20 58 22)"/></svg>',
    pao: '<svg viewBox="0 0 100 100" aria-hidden="true"><ellipse cx="50" cy="56" rx="40" ry="22" fill="#D99A4E"/><path d="M30 46 l8 14 M46 42 l8 16 M62 44 l8 14" stroke="#A86A26" stroke-width="5" stroke-linecap="round"/></svg>',
    iogurte: '<svg viewBox="0 0 100 100" aria-hidden="true"><path d="M30 30 h40 l-6 60 h-28 z" fill="#FFF8EA" stroke="#1A3A2A" stroke-width="4"/><rect x="26" y="22" width="48" height="10" rx="3" fill="#9CCC6C" stroke="#1A3A2A" stroke-width="4"/><path d="M36 56 h28" stroke="#C2410C" stroke-width="6"/></svg>',
    caixa: '<svg viewBox="0 0 100 100" aria-hidden="true"><rect x="14" y="40" width="72" height="44" rx="4" fill="#B97A3E"/><path d="M14 54 h72 M14 68 h72" stroke="#8C5C2B" stroke-width="4"/><circle cx="34" cy="36" r="12" fill="#D9452B"/><circle cx="56" cy="34" r="12" fill="#6BAE45"/><circle cx="72" cy="38" r="10" fill="#F29A2B"/></svg>'
  };

  function norm(s) {
    return String(s || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
  }

  /** Picks an illustration by description first, then by category. */
  function illustrationFor(categoria, descricao) {
    const d = norm(descricao);
    const pairs = [['tomat', 'tomate'], ['alface', 'alface'], ['couve', 'alface'], ['folha', 'alface'],
      ['cenour', 'cenoura'], ['banan', 'banana'], ['maca', 'maca'], ['laranj', 'laranja'], ['mamao', 'laranja'],
      ['pao', 'pao'], ['iogurte', 'iogurte'], ['queijo', 'iogurte'], ['leite', 'iogurte'], ['abobr', 'cenoura']];
    for (const [k, v] of pairs) if (d.includes(k)) return ILL[v];
    const c = norm(categoria);
    if (c.includes('fruta')) return ILL.maca;
    if (c.includes('verdura')) return ILL.alface;
    if (c.includes('legume')) return ILL.tomate;
    if (c.includes('padaria')) return ILL.pao;
    if (c.includes('latic')) return ILL.iogurte;
    return ILL.caixa;
  }
  const tileClass = (id) => 't' + ((Number(id || 0) % 4) + 1);

  // ---------- Icons (stroke SVG; always used next to a word) ----------
  const P = {
    check: '<path d="M5 12l5 5 9-10"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    pin: '<path d="M12 21s-7-6.5-7-12a7 7 0 0 1 14 0c0 5.5-7 12-7 12z"/><circle cx="12" cy="9" r="2.5"/>',
    bell: '<path d="M6 16V11a6 6 0 0 1 12 0v5l2 2H4z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
    basket: '<path d="M3 10h18l-2 10H5z"/><path d="M8 10l4-6 4 6"/>',
    store: '<path d="M4 10l1-5h14l1 5"/><path d="M5 10v10h14V10"/><path d="M10 20v-5h4v5"/>',
    chat: '<path d="M4 20l1.5-4A8 8 0 1 1 8 19z"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    sprout: '<path d="M12 21v-9"/><path d="M12 12C12 7 8 5 4 5c0 4 3 7 8 7z"/><path d="M12 14c0-4 3-6 7-6 0 4-3 6-7 6z"/>',
    trash: '<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/>',
    edit: '<path d="M4 20h4L20 8l-4-4L4 16z"/>',
    x: '<path d="M6 6l12 12M18 6L6 18"/>',
    download: '<path d="M12 4v11M7 10l5 5 5-5M5 20h14"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
    leaf: '<path d="M5 19C5 9 12 5 20 4c0 9-5 15-15 15z"/><path d="M5 19l8-8"/>'
  };
  function icon(name, cls) {
    return '<svg class="ico ' + (cls || '') + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
      'stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + (P[name] || '') + '</svg>';
  }

  const TIPO_BEN = { PESSOA: 'Pessoa', FAMILIA: 'Família', INSTITUICAO: 'Instituição', PEQUENO_NEGOCIO: 'Pequeno negócio' };
  const CONDICAO = { BOA: 'Boa', MADURA: 'Madura', CONSUMO_IMEDIATO: 'Feia, mas boa' };
  const STATUS_LOTE = { DISPONIVEL: 'Disponível', RESERVADO: 'Reservado', RETIRADO: 'Retirado', EXPIRADO: 'Venceu' };

  window.RP = {
    api, qs, esc, fmtKg, fmtNum, fmtBRL, fmtHora, fmtDia, fmtPrazo, fmtData, isoLocal, parseLocal,
    illustrationFor, tileClass, icon, TIPO_BEN, CONDICAO, STATUS_LOTE
  };
})();
