/* Rede de Prato — Painel de impacto + selo (HU15, HU20). */
(function () {
  'use strict';
  const { api, qs, esc } = RP;
  const $ = (id) => document.getElementById(id);
  const MESES = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro',
    'outubro', 'novembro', 'dezembro'];
  const state = { ofertante: null, mes: '', dados: null };

  function mesExtenso(ym) {
    const [y, m] = String(ym).split('-');
    return MESES[Number(m) - 1] + ' de ' + y;
  }

  function erro(m) {
    $('erro').textContent = m || '';
    $('erro').hidden = !m;
  }

  function renderBars(semanas) {
    const max = Math.max(1, ...semanas.map((s) => Number(s.kg)));
    $('bars').innerHTML = semanas.map((s, i) => {
      const h = Math.round((Number(s.kg) / max) * 85);
      return '<div class="bar' + (i === semanas.length - 1 ? ' atual' : '') + '"><span class="val">' +
        esc(RP.fmtKg(s.kg)) + '</span><div class="b" style="height:' + h + '%"></div></div>';
    }).join('');
    $('bars').setAttribute('aria-label', 'Quilos salvos por semana: ' + semanas.map((s, i) =>
      (i === semanas.length - 1 ? 'esta semana ' : 'semana ' + (i + 1) + ' ') + RP.fmtKg(s.kg)).join(', '));
  }

  function renderSplit(d) {
    const total = Number(d.totalKg);
    const pc = total > 0 ? Number(d.percentualConsumo) : 0;
    const pa = total > 0 ? Math.round((100 - pc) * 10) / 10 : 0;
    $('split').innerHTML = total > 0
      ? '<div class="c" style="width:' + pc + '%">' + esc(RP.fmtNum(pc)) + '% comida</div>' +
        '<div class="a" style="width:' + pa + '%">' + (pa > 0 ? esc(RP.fmtNum(pa)) + '%' : '') + '</div>'
      : '<div class="c" style="width:100%;background:var(--desabilitado);color:var(--cinza-texto)">Nada registrado neste mês</div>';
  }

  function seloSvg(nome) {
    return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 400" width="400" height="400">' +
      '<circle cx="200" cy="200" r="190" fill="#FAEFD9"/><circle cx="200" cy="200" r="175" fill="#1A3A2A"/>' +
      '<rect x="125" y="95" width="150" height="100" rx="14" fill="#DCEFC7" stroke="#9CCC6C" stroke-width="6"/>' +
      '<circle cx="200" cy="145" r="32" fill="#9CCC6C"/><circle cx="200" cy="145" r="20" fill="#DCEFC7"/>' +
      '<rect x="150" y="115" width="8" height="60" rx="4" fill="#9CCC6C"/><rect x="242" y="115" width="8" height="60" rx="4" fill="#9CCC6C"/>' +
      '<text x="200" y="250" text-anchor="middle" font-family="Arial Black, Arial, sans-serif" font-size="34" fill="#FFF8EA">PARCEIRO</text>' +
      '<text x="200" y="282" text-anchor="middle" font-family="Arial, sans-serif" font-weight="bold" font-size="18" fill="#9CCC6C" letter-spacing="3">REDE DE PRATO</text>' +
      '<text x="200" y="318" text-anchor="middle" font-family="Arial, sans-serif" font-size="15" fill="#FFF8EA">' +
      esc(nome) + '</text></svg>';
  }

  function baixarSelo() {
    const blob = new Blob([seloSvg(state.ofertante.nome)], { type: 'image/svg+xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'selo-rede-de-prato.svg';
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  function renderSelo(d) {
    const host = $('selo');
    if (d.possuiSelo) {
      const texto = 'Este mês, a ' + d.nomeOfertante + ' salvou ' + RP.fmtKg(d.totalKg) + ' de comida do lixo com a Rede de Prato.';
      host.innerHTML = '<span class="hand">você é parceiro</span>' +
        '<div class="selo" role="img" aria-label="Selo de Parceiro Rede de Prato"><img src="/img/logo_light.png" alt="" style="width:90px">PARCEIRO<small>REDE DE PRATO</small></div>' +
        '<p class="muted" style="margin:0;color:var(--verde-escuro)">Parceiro desde ' + esc(String(d.dataSelo).split('-').reverse().join('/')) + '</p>' +
        '<p class="quote">“Este mês, a ' + esc(d.nomeOfertante) + ' salvou <strong>' + esc(RP.fmtKg(d.totalKg)) + '</strong> de comida do lixo.”</p>' +
        '<a class="btn btn-dark btn-block" target="_blank" rel="noopener" href="https://wa.me/?text=' +
        encodeURIComponent(texto) + '">' + RP.icon('chat') + 'Mandar no WhatsApp</a>' +
        '<button type="button" class="btn btn-block" style="background:var(--creme-claro)" id="btn-baixar">' + RP.icon('download') + 'Baixar o selo</button>';
      $('btn-baixar').addEventListener('click', baixarSelo);
    } else {
      host.innerHTML = '<span class="hand">selo de parceiro</span><h2 style="font-size:30px">Ainda sem selo</h2>' +
        '<p>Doe com frequência pela Rede de Prato e registre para onde foi a comida. ' +
        'A equipe da Rede de Prato concede o <strong>Selo de Parceiro</strong> para quem participa.</p>';
    }
  }

  async function loadOfertante() {
    if (!state.ofertante) return;
    erro('');
    try {
      const d = await api.get(qs('/api/impacto/ofertantes/' + state.ofertante.idOfertante, { mes: state.mes }));
      state.dados = d;
      $('mes-titulo').textContent = mesExtenso(d.mes);
      if (!state.mes) $('mes').value = d.mes;
      $('total').textContent = RP.fmtNum(d.totalKg);
      $('consumo').textContent = RP.fmtNum(d.consumoKg);
      $('adubo').textContent = RP.fmtNum(d.compostagemKg);
      $('familias').textContent = d.beneficiariosAtendidos;
      $('pessoas').textContent = d.pessoasAtendidas ? '(' + d.pessoasAtendidas + ' pessoas)' : '';
      $('lotes').textContent = d.lotesRetirados;
      renderBars(d.porSemana);
      renderSplit(d);
      renderSelo(d);
    } catch (e) {
      erro(e.message);
    }
  }

  async function loadRede() {
    try {
      const r = await api.get(qs('/api/impacto/rede', { mes: state.mes }));
      const card = (v, t) => '<div class="card stat-card" style="padding:18px"><p class="v" style="font-size:36px">' + esc(v) + '</p><p style="margin:0">' + t + '</p></div>';
      $('rede-cards').innerHTML = card(RP.fmtKg(r.totalKg), 'salvos do lixo') + card(RP.fmtNum(r.percentualConsumo) + '%', 'virou <strong>comida</strong>') +
        card(r.beneficiariosAtendidos, '<strong>famílias</strong> e instituições') + card(r.lotesRetirados, '<strong>lotes</strong> retirados');
      $('rede-rank').innerHTML = r.ofertantes.length
        ? r.ofertantes.map((o, i) => '<li><span>' + (i + 1) + '. ' + esc(o.nome) + '</span><strong>' + esc(RP.fmtKg(o.totalKg)) + '</strong></li>').join('')
        : '<li>Nenhum destino registrado neste mês.</li>';
    } catch (e) { /* the main panel already shows errors */ }
  }

  async function loadAdmin() {
    const lista = await api.get('/api/ofertantes?ativo=true').catch(() => []);
    $('admin-of').innerHTML = lista.map((o) => '<option value="' + o.idOfertante + '">' + esc(o.nome) +
      (o.possuiSelo ? ' (já tem selo)' : '') + '</option>').join('');
  }

  async function concederSelo() {
    const id = $('admin-of').value;
    const out = $('admin-msg');
    if (!id) return;
    try {
      const o = await api.put('/api/ofertantes/' + id + '/selo');
      out.innerHTML = '<span class="msg-ok" style="display:inline-block">Selo concedido para ' + esc(o.nome) + '.</span>';
      loadAdmin();
      if (state.ofertante && String(state.ofertante.idOfertante) === String(id)) loadOfertante();
    } catch (e) {
      out.innerHTML = '<span class="msg-erro" style="display:inline-block">' + esc(e.message) + '</span>';
    }
  }

  async function start() {
    RP.icons();
    api.get('/api/health').then((h) => { $('badge-exemplo').hidden = h.profile === 'oracle'; }).catch(() => {});
    $('mes').addEventListener('change', (e) => { state.mes = e.target.value; loadOfertante(); loadRede(); });
    $('btn-selo').addEventListener('click', concederSelo);
    state.ofertante = await Persona.init({ kind: 'ofertante', onChange: (p) => { state.ofertante = p; loadOfertante(); } });
    loadOfertante();
    loadRede();
    loadAdmin();
  }

  start();
})();
