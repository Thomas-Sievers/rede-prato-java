/* Rede de Prato — Lotes disponíveis + reserva (HU05, HU09, HU10, HU12). */
(function () {
  'use strict';
  const { api, qs, esc } = RP;
  const state = { beneficiario: null, idCategoria: '', modalidade: '', bairro: '', lotes: [], pontos: [] };
  const $ = (id) => document.getElementById(id);

  function showError(msg) {
    const el = $('erro');
    el.textContent = msg || '';
    el.hidden = !msg;
  }

  function lista(itens) {
    if (itens.length <= 1) return itens.join('');
    return itens.slice(0, -1).join(', ') + ' e ' + itens[itens.length - 1];
  }

  // ---------- Avisos (HU05) ----------
  async function loadAlertas() {
    let alertas = [];
    try { alertas = await api.get('/api/alertas/recentes?horas=24'); } catch (e) { return; }
    const xepas = alertas.filter((a) => a.tipo === 'XEPA');
    const banner = $('banner-xepa');
    if (xepas.length) {
      const of = xepas[0].nomeOfertante;
      const daBanca = xepas.filter((a) => a.nomeOfertante === of);
      const descs = [...new Set(daBanca.map((a) => String(a.descricaoLote).toLowerCase()))];
      const prazo = daBanca.map((a) => a.dataLimiteRetirada).sort()[daBanca.length - 1];
      banner.innerHTML = RP.icon('bell') + '<p style="margin:0"><strong>Xepa agora!</strong> A ' + esc(of) +
        ' tem ' + esc(lista(descs)) + ' até as ' + esc(RP.fmtHora(prazo)) + '.</p>';
      banner.hidden = false;
    } else {
      banner.hidden = true;
    }
    const novos = alertas.filter((a) => a.tipo === 'NOVO_LOTE');
    const aviso = $('aviso-novo');
    if (novos.length) {
      const n = novos[0];
      aviso.innerHTML = RP.icon('leaf') + ' <strong>Novidade:</strong> ' + esc(n.descricaoLote) + ' na ' +
        esc(n.nomeOfertante) + (novos.length > 1 ? ' e mais ' + (novos.length - 1) + ' aviso(s) hoje.' : '.');
      aviso.hidden = false;
    } else {
      aviso.hidden = true;
    }
  }

  // ---------- Filtros (HU09) ----------
  async function loadFiltros() {
    const [cats, ofertantes] = await Promise.all([
      api.get('/api/categorias').catch(() => []),
      api.get('/api/ofertantes?ativo=true').catch(() => [])
    ]);
    const chips = [{ id: '', nome: 'Tudo', ill: RP.icon('basket') }]
      .concat(cats.map((c) => ({ id: c.idCategoria, nome: c.nome, ill: RP.illustrationFor(c.nome, '') })));
    $('chips-cat').innerHTML = chips.map((c) => '<button type="button" class="chip" data-cat="' + c.id +
      '" aria-pressed="' + (String(c.id) === String(state.idCategoria)) + '"><span style="width:26px;display:inline-flex">' +
      c.ill + '</span>' + esc(c.nome) + '</button>').join('');
    const bairros = [...new Set(ofertantes.map((o) => o.bairro).filter(Boolean))].sort();
    $('sel-bairro').innerHTML = '<option value="">Todos os bairros</option>' +
      bairros.map((b) => '<option>' + esc(b) + '</option>').join('');

    $('chips-cat').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-cat]');
      if (!b) return;
      state.idCategoria = b.dataset.cat;
      $('chips-cat').querySelectorAll('button').forEach((x) => x.setAttribute('aria-pressed', String(x === b)));
      loadLotes();
    });
    $('chips-mod').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-mod]');
      if (!b) return;
      state.modalidade = b.dataset.mod;
      $('chips-mod').querySelectorAll('button').forEach((x) => x.setAttribute('aria-pressed', String(x === b)));
      loadLotes();
    });
    $('sel-bairro').addEventListener('change', (e) => { state.bairro = e.target.value; loadLotes(); });
  }

  function valorLinha(l) {
    if (l.modalidade === 'DOACAO') return '<span class="tag-gratis">De graça</span>';
    return '<span class="preco">' + RP.fmtBRL(l.preco) +
      (l.valorEstimado ? '<s>era ' + RP.fmtBRL(l.valorEstimado) + '</s>' : '') + '</span>';
  }

  function card(l) {
    return '<article class="card lote-card">' +
      '<div class="tile ' + RP.tileClass(l.idLote) + '"><span class="pill-kg">' + esc(RP.fmtKg(l.pesoKg)) + '</span>' +
      RP.illustrationFor(l.nomeCategoria, l.descricao) + '</div>' +
      '<h2 class="card-title">' + esc(l.descricao) + '</h2>' +
      '<div>' + valorLinha(l) + '</div>' +
      '<p class="line">' + RP.icon('pin') + esc(l.nomeOfertante) + ' · ' + esc(l.bairro || '') + '</p>' +
      '<p class="line">' + RP.icon('clock') + 'Buscar ' + esc(RP.fmtPrazo(l.dataLimiteRetirada)) + '</p>' +
      // A9: each button gets a unique accessible name
      '<button type="button" class="btn btn-dark btn-block" data-reservar="' + l.idLote + '" aria-label="' +
      esc('Reservar ' + l.descricao + ', ' + RP.fmtKg(l.pesoKg)) + '">Reservar</button>' +
      '</article>';
  }

  async function loadLotes() {
    showError('');
    try {
      state.lotes = await api.get(qs('/api/lotes/disponiveis', {
        idCategoria: state.idCategoria, modalidade: state.modalidade, bairro: state.bairro
      }));
    } catch (e) {
      showError(e.message);
      state.lotes = [];
    }
    $('lista').innerHTML = state.lotes.map(card).join('');
    const vazio = $('vazio');
    if (state.lotes.length) {
      vazio.hidden = true;
      return;
    }
    const filtrado = state.idCategoria || state.modalidade || state.bairro;
    if (filtrado) {
      vazio.innerHTML = '<p class="empty">Nada deste tipo agora. Toque em <strong>Tudo</strong> para ver o resto.</p>';
    } else {
      vazio.innerHTML = '<div class="empty"><p><strong>Nenhum alimento disponível agora.</strong> ' +
        'Você também pode buscar nos pontos com a placa:</p>' +
        state.pontos.map((p) => '<p class="line" style="justify-content:center">' + RP.icon('pin') +
          esc(p.nome) + ' · ' + esc(p.diasFuncionamento) + ', ' + esc(p.horarioAbertura) + ' às ' +
          esc(p.horarioFechamento) + '</p>').join('') + '</div>';
    }
    vazio.hidden = false;
  }

  // ---------- Reserva (HU10 / UC12) ----------
  function abrirReserva(lote) {
    if (!state.beneficiario) {
      showError('Escolha quem você é no topo da página.');
      return;
    }
    const enderecoOf = lote.logradouro + (lote.numero ? ', ' + lote.numero : '') + ' - ' + (lote.bairro || '');
    const opcoes = [{ id: '', titulo: 'No próprio lugar', sub: lote.nomeOfertante + ' · ' + enderecoOf }]
      .concat(state.pontos.map((p) => ({
        id: p.idPonto, titulo: 'No ponto da placa',
        sub: p.nome + ' · ' + p.diasFuncionamento + ', ' + p.horarioAbertura + ' às ' + p.horarioFechamento
      })));
    const modal = RP.modal.open(
      '<span class="hand">você escolheu</span>' +
      '<h2 id="modal-title">' + esc(lote.descricao) + ' · ' + esc(RP.fmtKg(lote.pesoKg)) + '</h2>' +
      '<p>' + valorLinha(lote) + (lote.modalidade === 'VENDA' ? ' <span class="muted">· pague na hora de buscar</span>' : '') + '</p>' +
      '<p class="line">' + RP.icon('clock') + 'Buscar ' + esc(RP.fmtPrazo(lote.dataLimiteRetirada)) + '</p>' +
      '<h3 id="onde" style="margin:20px 0 12px">Onde você quer buscar?</h3>' +
      '<div role="radiogroup" aria-labelledby="onde" style="display:flex;flex-direction:column;gap:12px">' +
      opcoes.map((o, i) => '<button type="button" class="option" role="radio" aria-checked="false" tabindex="' +
        (i === 0 ? '0' : '-1') + '" data-ponto="' + o.id + '" aria-label="' + esc(o.titulo + ': ' + o.sub) + '"><strong>' + esc(o.titulo) + '</strong><span>' +
        esc(o.sub) + '</span></button>').join('') +
      '</div>' +
      '<p class="msg-erro" role="alert" id="modal-erro" hidden style="margin-top:16px"></p>' +
      '<button type="button" class="btn btn-ok btn-block" id="btn-confirmar" disabled style="margin-top:20px">Escolha onde buscar</button>' +
      '<p style="text-align:center"><button type="button" class="link-btn" id="btn-outro">Escolher outro alimento</button></p>');

    let escolhido = null;
    const radios = [...modal.querySelectorAll('[role=radio]')];
    const confirmar = modal.querySelector('#btn-confirmar');
    function escolher(r) {
      radios.forEach((x) => { x.setAttribute('aria-checked', String(x === r)); x.tabIndex = x === r ? 0 : -1; });
      escolhido = r.dataset.ponto;
      confirmar.disabled = false;
      confirmar.textContent = 'Confirmar reserva';
    }
    radios.forEach((r, i) => {
      r.addEventListener('click', () => escolher(r));
      r.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowDown' || e.key === 'ArrowRight' || e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
          const d = (e.key === 'ArrowDown' || e.key === 'ArrowRight') ? 1 : -1;
          const n = radios[(i + d + radios.length) % radios.length];
          n.focus();
          escolher(n);
          e.preventDefault();
        }
      });
    });
    modal.querySelector('#btn-outro').addEventListener('click', () => RP.modal.close());
    confirmar.addEventListener('click', async () => {
      confirmar.disabled = true;
      try {
        const r = await api.post('/api/reservas', {
          idLote: lote.idLote, idBeneficiario: state.beneficiario.idBeneficiario,
          idPonto: escolhido ? Number(escolhido) : null
        });
        sucesso(r);
      } catch (e) {
        const el = modal.querySelector('#modal-erro');
        el.textContent = e.status === 409 ? 'Alguém já reservou este alimento. Escolha outro. (' + e.message + ')'
          : e.status === 400 && /prazo/i.test(e.message) ? 'O prazo deste alimento acabou.' : e.message;
        el.hidden = false;
        loadLotes();
      }
    });
  }

  function sucesso(r) {
    const local = r.localRetirada === 'PONTO'
      ? r.nomePonto + ' · ' + r.enderecoPonto + ' · ' + r.diasFuncionamento + ', ' + r.horarioAbertura + ' às ' + r.horarioFechamento
      : r.nomeOfertante + ' · ' + r.enderecoOfertante;
    const modal = RP.modal.open(
      '<div role="status" style="text-align:center">' +
      '<div style="width:84px;height:84px;margin:0 auto;border-radius:50%;background:var(--verde-medio);color:#fff;display:flex;align-items:center;justify-content:center">' +
      RP.icon('check') + '</div>' +
      '<h2 id="modal-title" style="margin:12px 0">Reservado!</h2>' +
      '<div class="box-dark"><p style="margin:0">Seu número</p><p class="numero" style="margin:4px 0">' + esc(r.numeroRetirada) + '</p>' +
      '<p style="margin:0">Mostre este número na hora de buscar</p></div>' +
      '<p class="line" style="justify-content:center">' + RP.icon('pin') + esc(local) + '</p>' +
      '<p class="line" style="justify-content:center">' + RP.icon('clock') + 'Buscar ' + esc(RP.fmtPrazo(r.dataLimiteRetirada)) + '</p>' +
      (r.pagamentoNaRetirada ? '<p><strong>Valor: ' + esc(RP.fmtBRL(r.preco)) + '</strong> — pague na hora de buscar.</p>' : '') +
      '<p class="muted">Também mandamos tudo no seu WhatsApp.</p>' +
      '<button type="button" class="btn btn-dark" id="btn-mais">Ver mais comida</button></div>',
      { onClose: refresh });
    modal.querySelector('#btn-mais').addEventListener('click', () => RP.modal.close());
  }

  // ---------- Sacola / Minhas reservas (HU12) ----------
  async function loadSacola() {
    if (!state.beneficiario) return [];
    let ativas = [];
    try {
      ativas = await api.get(qs('/api/reservas', { idBeneficiario: state.beneficiario.idBeneficiario, status: 'ATIVA' }));
    } catch (e) { /* ignore */ }
    $('sacola-txt').textContent = ativas.length === 0 ? 'vazia' : ativas.length === 1 ? '1 reserva' : ativas.length + ' reservas';
    return ativas;
  }

  async function abrirSacola() {
    const ativas = await loadSacola();
    const modal = RP.modal.open(
      '<span class="hand">sua sacola</span><h2 id="modal-title">Minhas reservas</h2>' +
      '<p class="msg-erro" role="alert" id="sacola-erro" hidden></p>' +
      (ativas.length ? ativas.map((r) => '<div class="card" style="margin-top:16px">' +
        '<h3>' + esc(r.descricaoLote) + ' · ' + esc(RP.fmtKg(r.pesoKg)) + '</h3>' +
        '<p>Número <strong>' + esc(r.numeroRetirada) + '</strong></p>' +
        '<p class="line">' + RP.icon('pin') + esc(r.localRetirada === 'PONTO' ? r.nomePonto : r.nomeOfertante) + '</p>' +
        '<p class="line">' + RP.icon('clock') + 'Buscar ' + esc(RP.fmtPrazo(r.dataLimiteRetirada)) + '</p>' +
        '<button type="button" class="btn btn-sm" data-cancelar="' + r.idReserva + '">' + RP.icon('x') + 'Cancelar reserva</button>' +
        '</div>').join('')
        : '<p class="empty" style="margin-top:16px">Sua sacola está vazia.</p>'),
      { onClose: refresh });
    modal.addEventListener('click', async (e) => {
      const b = e.target.closest('[data-cancelar]');
      if (!b) return;
      b.disabled = true;
      try {
        await api.put('/api/reservas/' + b.dataset.cancelar + '/cancelar');
        b.closest('.card').innerHTML = '<p class="msg-ok" role="status">Reserva cancelada. O alimento voltou para a lista.</p>';
        loadSacola();
      } catch (err) {
        const el = modal.querySelector('#sacola-erro');
        el.textContent = err.message;
        el.hidden = false;
        b.disabled = false;
      }
    });
  }

  function refresh() {
    loadLotes();
    loadSacola();
    loadAlertas();
  }

  async function start() {
    RP.icons();
    $('lista').addEventListener('click', (e) => {
      const b = e.target.closest('[data-reservar]');
      if (!b) return;
      const lote = state.lotes.find((l) => String(l.idLote) === b.dataset.reservar);
      if (lote) abrirReserva(lote);
    });
    $('btn-sacola').addEventListener('click', abrirSacola);
    state.pontos = await api.get('/api/pontos?ativo=true').catch(() => []);
    state.beneficiario = await Persona.init({
      kind: 'beneficiario',
      onChange: (p) => { state.beneficiario = p; loadSacola(); }
    });
    await loadFiltros();
    refresh();
  }

  start();
})();
