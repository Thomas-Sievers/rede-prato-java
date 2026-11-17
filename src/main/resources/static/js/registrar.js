/* Rede de Prato — Registrar lote + xepa (HU02, HU03, HU04). */
(function () {
  'use strict';
  const { api, qs, esc } = RP;
  const $ = (id) => document.getElementById(id);
  const state = {
    ofertante: null, categorias: [], idCategoria: null, peso: 3, condicao: 'MADURA', modalidade: 'DOACAO',
    prazo: null, editId: null, editPrazo: null, ultimosAlertas: []
  };

  // ---------- helpers ----------
  function pressOnly(group, btn) {
    group.querySelectorAll('[aria-pressed]').forEach((b) => b.setAttribute('aria-pressed', String(b === btn)));
  }

  function prazoDe(code) {
    const now = new Date();
    const d = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    if (code === 'h14') d.setHours(14);
    else if (code === 'h18') d.setHours(18);
    else { d.setDate(d.getDate() + 1); d.setHours(10); }
    return d;
  }

  function atualizaChipsPrazo() {
    const agora = new Date();
    let primeiroValido = null;
    $('chips-prazo').querySelectorAll('button').forEach((b) => {
      const passou = prazoDe(b.dataset.p) <= agora;
      b.disabled = passou;
      b.title = passou ? 'Esse horário já passou' : '';
      if (!passou && !primeiroValido) primeiroValido = b;
    });
    if (!state.prazo || prazoDe(state.prazo) <= agora) {
      state.prazo = primeiroValido ? primeiroValido.dataset.p : 'a10';
    }
    $('chips-prazo').querySelectorAll('button').forEach((b) =>
      b.setAttribute('aria-pressed', String(!state.editPrazo && b.dataset.p === state.prazo)));
  }

  function prazoEscolhido() {
    return state.editPrazo ? RP.parseLocal(state.editPrazo) : prazoDe(state.prazo);
  }

  function nomeCategoria() {
    const c = state.categorias.find((x) => x.idCategoria === state.idCategoria);
    return c ? c.nome : '';
  }

  function numOrNull(v) {
    if (v === '' || v === null || v === undefined) return null;
    const n = Number(String(v).replace(',', '.'));
    return Number.isFinite(n) ? n : null;
  }

  // ---------- preview (step 7) ----------
  function renderPreview() {
    const desc = $('descricao').value.trim() || 'Seu alimento';
    const cond = { BOA: 'Boa · bonita e firme', MADURA: 'Madura · comer logo', CONSUMO_IMEDIATO: 'Feia, mas boa · machucada por fora' }[state.condicao];
    const preco = numOrNull($('preco').value);
    const valor = numOrNull($('valor').value);
    const valorHtml = state.modalidade === 'DOACAO' ? '<span class="tag-gratis">De graça</span>'
      : '<span class="preco">' + RP.fmtBRL(preco || 0) + (valor ? '<s>era ' + RP.fmtBRL(valor) + '</s>' : '') + '</span>';
    $('preview').innerHTML =
      '<div class="tile t1"><span class="pill-kg">' + esc(RP.fmtKg(state.peso)) + '</span>' +
      RP.illustrationFor(nomeCategoria(), desc) + '</div>' +
      '<div class="body"><h3>' + esc(desc) + '</h3><div>' + valorHtml + '</div>' +
      '<p class="muted" style="margin:0">' + esc(cond) + ' · ' + esc(state.ofertante ? state.ofertante.nome : '') +
      ' · buscar ' + esc(RP.fmtPrazo(RP.isoLocal(prazoEscolhido()))) + '</p></div>';
  }

  // ---------- form ----------
  function renderCategorias() {
    $('tiles-cat').innerHTML = state.categorias.map((c) =>
      '<button type="button" class="option" data-id="' + c.idCategoria + '" aria-pressed="' +
      (c.idCategoria === state.idCategoria) + '">' + RP.illustrationFor(c.nome, '') + '<strong>' + esc(c.nome) +
      '</strong></button>').join('');
  }

  function setPeso(v) {
    state.peso = Math.max(1, Math.round(v * 100) / 100);
    $('peso').textContent = RP.fmtNum(state.peso);
    renderPreview();
  }

  function setModalidade(m) {
    state.modalidade = m;
    $('opt-mod').querySelectorAll('[data-v]').forEach((b) => b.setAttribute('aria-pressed', String(b.dataset.v === m)));
    $('campo-preco').hidden = m !== 'VENDA';
    $('label-valor').textContent = m === 'VENDA' ? 'Preço normal (R$)' : 'Valor estimado (R$) — se souber';
    renderPreview();
  }

  function setCondicao(c) {
    state.condicao = c;
    $('opt-cond').querySelectorAll('[data-v]').forEach((b) => b.setAttribute('aria-pressed', String(b.dataset.v === c)));
    renderPreview();
  }

  function limparForm() {
    state.editId = null;
    state.editPrazo = null;
    $('descricao').value = '';
    $('preco').value = '';
    $('valor').value = '';
    $('foto').value = '';
    $('transporte').checked = false;
    setPeso(3);
    setCondicao('MADURA');
    setModalidade('DOACAO');
    $('form-title').textContent = 'Registrar sobra';
    $('txt-enviar').textContent = 'Avisar as famílias';
    $('edit-info').hidden = true;
    $('btn-cancelar-edicao').hidden = true;
    atualizaChipsPrazo();
    renderPreview();
  }

  function editar(l) {
    state.editId = l.idLote;
    state.editPrazo = l.dataLimiteRetirada;
    state.idCategoria = l.idCategoria;
    renderCategorias();
    $('descricao').value = l.descricao;
    setPeso(Number(l.pesoKg));
    setCondicao(l.condicao);
    setModalidade(l.modalidade);
    $('preco').value = l.preco == null ? '' : l.preco;
    $('valor').value = l.valorEstimado == null ? '' : l.valorEstimado;
    $('foto').value = l.urlFoto || '';
    $('transporte').checked = !!l.precisaTransporte;
    $('form-title').textContent = 'Mudar lote';
    $('txt-enviar').textContent = 'Salvar mudança';
    $('edit-info').textContent = 'Você está mudando o lote "' + l.descricao + '" (prazo atual: ' +
      RP.fmtPrazo(l.dataLimiteRetirada) + '). Toque num prazo para trocar.';
    $('edit-info').hidden = false;
    $('btn-cancelar-edicao').hidden = false;
    atualizaChipsPrazo();
    renderPreview();
    $('form').scrollIntoView();
    $('descricao').focus();
  }

  async function enviar(e) {
    e.preventDefault();
    const erro = $('form-erro');
    erro.hidden = true;
    $('form-ok').innerHTML = '';
    if (!state.ofertante) { erro.textContent = 'Escolha quem você é no topo da página.'; erro.hidden = false; return; }
    if (!state.idCategoria) { erro.textContent = 'Escolha o tipo de alimento (passo 1).'; erro.hidden = false; return; }
    const body = {
      idOfertante: state.ofertante.idOfertante,
      idCategoria: state.idCategoria,
      descricao: $('descricao').value,
      pesoKg: state.peso,
      condicao: state.condicao,
      modalidade: state.modalidade,
      preco: state.modalidade === 'VENDA' ? numOrNull($('preco').value) : null,
      valorEstimado: numOrNull($('valor').value),
      dataLimiteRetirada: RP.isoLocal(prazoEscolhido()),
      urlFoto: $('foto').value.trim() || null,
      precisaTransporte: $('transporte').checked
    };
    const btn = $('btn-enviar');
    btn.disabled = true;
    try {
      const editando = !!state.editId;
      const l = editando ? await api.put('/api/lotes/' + state.editId, body) : await api.post('/api/lotes', body);
      $('form-ok').innerHTML = '<div class="msg-ok"><p style="margin:0"><strong>' +
        (editando ? 'Lote alterado.' : 'Obrigado! As famílias foram avisadas.') + '</strong> ' +
        esc(l.descricao) + ' · ' + esc(RP.fmtKg(l.pesoKg)) + ' · buscar ' + esc(RP.fmtPrazo(l.dataLimiteRetirada)) +
        '</p><div class="chips" style="margin-top:12px"><button type="button" class="btn btn-sm" id="btn-outra">Registrar outra sobra</button>' +
        '<a class="btn btn-sm btn-dark" href="' + esc('/retiradas.html?ofertante=' + state.ofertante.idOfertante) + '">Ver retiradas</a></div></div>';
      $('btn-outra').addEventListener('click', () => { $('form-ok').innerHTML = ''; limparForm(); $('descricao').focus(); });
      limparForm();
      loadMeus();
    } catch (err) {
      erro.textContent = err.message;
      erro.hidden = false;
    } finally {
      btn.disabled = false;
    }
  }

  // ---------- meus lotes (HU03) ----------
  async function loadMeus() {
    if (!state.ofertante) return;
    const host = $('meus');
    let lotes = [];
    try { lotes = await api.get(qs('/api/lotes', { idOfertante: state.ofertante.idOfertante })); } catch (e) {
      host.innerHTML = '<p class="msg-erro">' + esc(e.message) + '</p>';
      return;
    }
    state.meus = lotes;
    if (!lotes.length) { host.innerHTML = '<p class="empty">Nenhum lote registrado ainda.</p>'; return; }
    const cls = { DISPONIVEL: 'badge-verde', RESERVADO: 'badge-amarelo', RETIRADO: 'badge-cinza', EXPIRADO: 'badge-laranja' };
    host.innerHTML = lotes.map((l) => '<article class="card meu">' +
      '<div><h3>' + esc(l.descricao) + ' · ' + esc(RP.fmtKg(l.pesoKg)) + '</h3>' +
      '<p class="line" style="margin:4px 0 0;flex-wrap:wrap">' + RP.icon('clock') + 'buscar ' + esc(RP.fmtPrazo(l.dataLimiteRetirada)) +
      ' · <span class="badge ' + cls[l.status] + '">' + esc(RP.STATUS_LOTE[l.status]) + '</span></p></div>' +
      (l.status === 'DISPONIVEL'
        ? '<div class="chips"><button type="button" class="btn btn-sm" data-mudar="' + l.idLote + '" aria-label="' +
          esc('Mudar ' + l.descricao) + '">' + RP.icon('edit') + 'Mudar</button>' +
          '<button type="button" class="btn btn-sm" data-excluir="' + l.idLote + '" aria-label="' +
          esc('Cancelar ' + l.descricao) + '">' + RP.icon('trash') + 'Cancelar</button></div>'
        : '') + '</article>').join('');
  }

  // ---------- xepa (HU04 / UC04) ----------
  async function avisarXepa() {
    const msg = $('xepa-msg');
    const btn = $('btn-xepa');
    btn.disabled = true;
    try {
      const r = await api.post('/api/alertas/xepa', { idOfertante: state.ofertante.idOfertante });
      state.ultimosAlertas = r.alertas.map((a) => a.idAlerta);
      const ate = r.alertas.map((a) => a.dataLimiteRetirada).sort().pop();
      msg.innerHTML = '<p style="margin:0"><strong>Pronto!</strong> ' + esc(r.mensagem.replace(/\.$/, '')) +
        ' até as ' + esc(RP.fmtHora(ate)) + '. <button type="button" class="link-btn" style="color:#fff" id="btn-desfazer">Desfazer</button></p>';
      $('btn-desfazer').addEventListener('click', desfazer);
    } catch (e) {
      msg.innerHTML = '<p style="margin:0">' + esc(/pelo menos um lote/i.test(e.message)
        ? 'Você não tem comida registrada agora. Registre abaixo.' : e.message) + '</p>';
    } finally {
      btn.disabled = false;
    }
  }

  async function desfazer() {
    const ids = state.ultimosAlertas;
    state.ultimosAlertas = [];
    try {
      await Promise.all(ids.map((id) => api.del('/api/alertas/' + id)));
      $('xepa-msg').innerHTML = '<p style="margin:0">Aviso de xepa desfeito.</p>';
    } catch (e) {
      $('xepa-msg').innerHTML = '<p style="margin:0">' + esc(e.message) + '</p>';
    }
  }

  function onPersona(p) {
    state.ofertante = p;
    $('xepa').hidden = !(p && p.tipo === 'FEIRANTE');
    $('xepa-msg').innerHTML = '';
    limparForm();
    loadMeus();
  }

  async function start() {
    RP.icons();
    state.categorias = await api.get('/api/categorias').catch(() => []);
    state.idCategoria = state.categorias.length ? state.categorias[0].idCategoria : null;
    renderCategorias();

    $('tiles-cat').addEventListener('click', (e) => {
      const b = e.target.closest('[data-id]');
      if (!b) return;
      state.idCategoria = Number(b.dataset.id);
      pressOnly($('tiles-cat'), b);
      renderPreview();
    });
    $('menos').addEventListener('click', () => setPeso(state.peso - 1));
    $('mais').addEventListener('click', () => setPeso(state.peso + 1));
    $('opt-cond').addEventListener('click', (e) => { const b = e.target.closest('[data-v]'); if (b) setCondicao(b.dataset.v); });
    $('opt-mod').addEventListener('click', (e) => { const b = e.target.closest('[data-v]'); if (b) setModalidade(b.dataset.v); });
    $('chips-prazo').addEventListener('click', (e) => {
      const b = e.target.closest('[data-p]');
      if (!b || b.disabled) return;
      state.prazo = b.dataset.p;
      state.editPrazo = null;
      pressOnly($('chips-prazo'), b);
      renderPreview();
    });
    ['descricao', 'preco', 'valor'].forEach((id) => $(id).addEventListener('input', renderPreview));
    $('form').addEventListener('submit', enviar);
    $('btn-cancelar-edicao').addEventListener('click', limparForm);
    $('btn-xepa').addEventListener('click', avisarXepa);
    $('meus').addEventListener('click', async (e) => {
      const m = e.target.closest('[data-mudar]');
      const x = e.target.closest('[data-excluir]');
      const erro = $('meus-erro');
      erro.hidden = true;
      if (m) {
        const l = (state.meus || []).find((v) => String(v.idLote) === m.dataset.mudar);
        if (l) editar(l);
      } else if (x) {
        if (!window.confirm('Cancelar este lote? Ele sai da lista das famílias.')) return;
        try {
          await api.del('/api/lotes/' + x.dataset.excluir);
          loadMeus();
        } catch (err) {
          erro.textContent = err.message;
          erro.hidden = false;
        }
      }
    });

    const p = await Persona.init({ kind: 'ofertante', onChange: onPersona });
    onPersona(p);
  }

  start();
})();
