/* Rede de Prato — Retiradas + destino (HU13, HU14). */
(function () {
  'use strict';
  const { api, qs, esc } = RP;
  const $ = (id) => document.getElementById(id);
  const state = { ofertante: null, reservas: [], destinos: {}, painel: null };

  const SPROUT = '<svg viewBox="0 0 120 80" width="160" aria-hidden="true"><path d="M10 78 Q60 58 110 78 z" fill="#8C5C2B"/>' +
    '<path d="M60 70 v-36" stroke="#2F6B2B" stroke-width="5"/><path d="M60 40 Q40 36 34 18 Q54 20 60 40 z" fill="#9CCC6C"/>' +
    '<path d="M60 36 Q70 16 88 12 Q84 32 60 36 z" fill="#7BB55A"/></svg>';

  function msg(text, ok) {
    $('msg').innerHTML = text ? '<span class="' + (ok ? 'msg-ok' : 'msg-erro') + '" style="display:inline-block">' + esc(text) + '</span>' : '';
  }

  function round2(n) { return Math.round(n * 100) / 100; }

  function resumoDestino(idLote) {
    const ds = state.destinos[idLote] || [];
    if (!ds.length) return null;
    const soma = (t) => ds.filter((d) => d.tipo === t).reduce((s, d) => s + Number(d.pesoKg), 0);
    const c = soma('CONSUMO');
    const a = soma('COMPOSTAGEM');
    return { total: round2(c + a), consumo: round2(c), adubo: round2(a) };
  }

  function card(r) {
    const local = r.localRetirada === 'PONTO' ? 'levado ao ponto ' + r.nomePonto : 'busca na banca';
    let acao;
    if (r.status === 'ATIVA') {
      acao = '<button type="button" class="btn btn-ok" data-buscou="' + r.idReserva + '">' + RP.icon('check') + 'Já buscou</button>';
    } else {
      const res = resumoDestino(r.idLote);
      acao = res
        ? '<span class="badge badge-verde">' + RP.icon('check') + esc(RP.fmtNum(res.total)) + ' kg: ' +
          esc(RP.fmtNum(res.consumo)) + ' comida, ' + esc(RP.fmtNum(res.adubo)) + ' adubo</span>'
        : '<button type="button" class="badge badge-amarelo" style="cursor:pointer;min-height:44px" data-destino="' +
          r.idReserva + '">Falta dizer o destino</button>';
    }
    return '<article class="card ret' + (r.status !== 'ATIVA' ? ' done' : '') + '" id="res-' + r.idReserva + '">' +
      '<div class="tile ' + RP.tileClass(r.idLote) + '">' + RP.illustrationFor(r.nomeCategoria, r.descricaoLote) + '</div>' +
      '<div><h3>' + esc(r.descricaoLote) + ' · ' + esc(RP.fmtKg(r.pesoKg)) + '</h3>' +
      '<p class="line" style="color:var(--verde-escuro)">' + RP.icon('user') + esc(r.nomeBeneficiario) + ' · ' +
      esc((RP.TIPO_BEN[r.tipoBeneficiario] || '').toLowerCase()) + '</p>' +
      '<p class="muted">Número <strong style="letter-spacing:.1em">' + esc(r.numeroRetirada) + '</strong> · ' + esc(local) +
      (r.status === 'ATIVA' ? ' · até ' + esc(RP.fmtDia(r.dataLimiteRetirada)) : '') + '</p></div>' +
      '<div>' + acao + '</div></article>';
  }

  async function load() {
    if (!state.ofertante) return;
    msg('');
    try {
      const [reservas, destinos] = await Promise.all([
        api.get(qs('/api/reservas', { idOfertante: state.ofertante.idOfertante })),
        api.get('/api/destinacoes')
      ]);
      state.reservas = reservas.filter((r) => r.status !== 'CANCELADA')
        .sort((a, b) => (a.status === 'ATIVA' ? 0 : 1) - (b.status === 'ATIVA' ? 0 : 1));
      state.destinos = {};
      destinos.filter((d) => d.idOfertante === state.ofertante.idOfertante).forEach((d) => {
        (state.destinos[d.idLote] = state.destinos[d.idLote] || []).push(d);
      });
    } catch (e) {
      msg(e.message);
      return;
    }
    $('lista').innerHTML = state.reservas.length ? state.reservas.map(card).join('')
      : '<p class="empty">Nenhuma reserva dos seus lotes ainda.</p>';
    loadVencidos();
  }

  async function loadVencidos() {
    let lotes = [];
    try {
      lotes = await api.get(qs('/api/lotes', { idOfertante: state.ofertante.idOfertante, status: 'EXPIRADO' }));
    } catch (e) { return; }
    const sem = lotes.filter((l) => !(state.destinos[l.idLote] || []).length);
    $('vencidos').innerHTML = sem.length
      ? '<span class="hand">não deu tempo</span><h2>Venceu sem retirada</h2>' +
        '<p class="muted">O que venceu só pode ir para o adubo.</p>' +
        sem.map((l) => '<article class="card ret done" style="margin-top:12px">' +
          '<div class="tile ' + RP.tileClass(l.idLote) + '">' + RP.illustrationFor(l.nomeCategoria, l.descricao) + '</div>' +
          '<div><h3>' + esc(l.descricao) + ' · ' + esc(RP.fmtKg(l.pesoKg)) + '</h3><p class="muted">Venceu ' +
          esc(RP.fmtData(l.dataLimiteRetirada)) + '</p></div>' +
          '<button type="button" class="btn btn-sm" data-adubo="' + l.idLote + '" data-kg="' + l.pesoKg + '">' +
          RP.icon('sprout') + 'Mandar para adubo</button></article>').join('')
      : '';
  }

  // ---------- painel de destino ----------
  function painelIdle() {
    state.painel = null;
    $('painel').innerHTML = '<div style="text-align:center">' + SPROUT + '<h2 style="font-size:28px;margin:12px 0">Para onde foi a comida?</h2>' +
      '<p>Quando a pessoa buscar, toque em <strong>JÁ BUSCOU</strong> e diga quanto virou comida e quanto virou adubo.</p></div>';
  }

  function abrirPainel(r) {
    const ja = resumoDestino(r.idLote);
    const restante = round2(Number(r.pesoKg) - (ja ? ja.total : 0));
    state.painel = { r, max: restante, comida: restante };
    renderPainel();
    $('painel').scrollIntoView({ block: 'nearest' });
  }

  function renderPainel() {
    const p = state.painel;
    const adubo = round2(p.max - p.comida);
    const pc = p.max > 0 ? (p.comida / p.max) * 100 : 0;
    $('painel').innerHTML =
      '<span class="hand">retirada confirmada</span>' +
      '<h2 style="font-size:28px" tabindex="-1" id="painel-title">O que aconteceu com os ' + esc(RP.fmtKg(p.max)) +
      ' de ' + esc(String(p.r.descricaoLote).toLowerCase()) + '?</h2>' +
      '<div class="split" style="margin:18px 0" aria-hidden="true"><div class="c" style="width:' + pc + '%"></div><div class="a" style="width:' + (100 - pc) + '%"></div></div>' +
      '<div class="row-dest"><strong>Virou comida</strong><div class="stepper" style="gap:10px">' +
      '<button type="button" class="round sm" id="c-menos" aria-label="Menos 1 quilo de comida">−</button>' +
      '<span class="big-num" aria-live="polite">' + esc(RP.fmtNum(p.comida)) + ' <small style="font-size:20px">kg</small></span>' +
      '<button type="button" class="round sm" id="c-mais" aria-label="Mais 1 quilo de comida">+</button></div></div>' +
      '<div class="row-dest"><strong style="color:var(--marrom-adubo)">Virou adubo</strong><span class="big-num" style="color:var(--marrom-adubo)" aria-live="polite">' +
      esc(RP.fmtNum(adubo)) + ' <small style="font-size:20px">kg</small></span></div>' +
      '<p class="muted">Adubo = o que não dava mais para comer. Vai para a horta comunitária.</p>' +
      '<p class="msg-erro" role="alert" id="painel-erro" hidden></p>' +
      '<button type="button" class="btn btn-ok btn-block" id="btn-salvar"' + (p.max <= 0 ? ' disabled' : '') + '>Salvar</button>';
    $('c-menos').addEventListener('click', () => { p.comida = round2(Math.max(0, p.comida - Math.min(1, p.comida))); renderPainel(); $('c-menos').focus(); });
    $('c-mais').addEventListener('click', () => { p.comida = round2(Math.min(p.max, p.comida + 1)); renderPainel(); $('c-mais').focus(); });
    $('btn-salvar').addEventListener('click', salvarDestino);
  }

  async function salvarDestino() {
    const p = state.painel;
    const btn = $('btn-salvar');
    btn.disabled = true;
    try {
      await api.post('/api/lotes/' + p.r.idLote + '/destinacoes', { consumoKg: p.comida, compostagemKg: round2(p.max - p.comida) });
      msg('Destino salvo: ' + RP.fmtNum(p.comida) + ' kg comida, ' + RP.fmtNum(round2(p.max - p.comida)) + ' kg adubo.', true);
      painelIdle();
      load();
    } catch (e) {
      const el = $('painel-erro');
      el.textContent = e.message;
      el.hidden = false;
      btn.disabled = false;
    }
  }

  // ---------- actions ----------
  async function jaBuscou(id) {
    try {
      const r = await api.put('/api/reservas/' + id + '/retirada');
      msg('Retirada confirmada: número ' + r.numeroRetirada + '.', true);
      await load();
      abrirPainel(r);
      const t = $('painel-title');
      if (t) t.focus();
    } catch (e) {
      msg(e.message);
    }
  }

  async function conferir(e) {
    e.preventDefault();
    const n = $('numero').value.trim();
    document.querySelectorAll('.ret.destaque').forEach((el) => el.classList.remove('destaque'));
    if (!n) { msg('Digite o número da pessoa.'); return; }
    try {
      const r = await api.get('/api/reservas/' + encodeURIComponent(n));
      if (r.idOfertante !== state.ofertante.idOfertante) {
        msg('O número ' + n + ' é de outra banca ou mercado.');
        return;
      }
      const el = $('res-' + r.idReserva);
      if (r.status === 'CANCELADA' || !el) {
        msg('O número ' + n + ' é de uma reserva cancelada.');
        return;
      }
      el.classList.add('destaque');
      el.scrollIntoView({ block: 'center' });
      msg('Número ' + n + ': ' + r.nomeBeneficiario + ' · ' + r.descricaoLote +
        (r.status === 'ATIVA' ? ' — pode entregar e tocar em JÁ BUSCOU.' : ' — já foi retirado.'), true);
      const b = el.querySelector('button');
      if (b) b.focus();
    } catch (err) {
      msg(err.status === 404 ? 'Não achamos o número ' + n + '.' : err.message);
    }
  }

  async function mandarAdubo(idLote, kg) {
    try {
      await api.post('/api/lotes/' + idLote + '/destinacoes', { compostagemKg: Number(kg) });
      msg('Enviado para adubo: ' + RP.fmtKg(kg) + '.', true);
      load();
    } catch (e) {
      msg(e.message);
    }
  }

  async function start() {
    RP.icons();
    painelIdle();
    $('form-conferir').addEventListener('submit', conferir);
    $('lista').addEventListener('click', (e) => {
      const b = e.target.closest('[data-buscou]');
      if (b) { b.disabled = true; jaBuscou(b.dataset.buscou); return; }
      const d = e.target.closest('[data-destino]');
      if (d) {
        const r = state.reservas.find((x) => String(x.idReserva) === d.dataset.destino);
        if (r) abrirPainel(r);
      }
    });
    $('vencidos').addEventListener('click', (e) => {
      const b = e.target.closest('[data-adubo]');
      if (b) { b.disabled = true; mandarAdubo(b.dataset.adubo, b.dataset.kg); }
    });
    state.ofertante = await Persona.init({
      kind: 'ofertante',
      onChange: (p) => { state.ofertante = p; painelIdle(); load(); }
    });
    load();
  }

  start();
})();
