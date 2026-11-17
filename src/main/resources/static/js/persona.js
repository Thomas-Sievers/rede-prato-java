/* Rede de Prato — "Você é:" selector (decision 5: no login). */
(function () {
  'use strict';

  function store(key, value) {
    try {
      if (value === undefined) return window.localStorage.getItem(key);
      window.localStorage.setItem(key, value);
    } catch (e) { /* storage blocked: URL param still works */ }
    return null;
  }

  function setUrlParam(name, value) {
    try {
      const u = new URL(window.location.href);
      u.searchParams.set(name, value);
      window.history.replaceState(null, '', u.toString());
    } catch (e) { /* ignore */ }
  }

  /**
   * Renders the selector inside #persona.
   * @param {{kind: 'beneficiario'|'ofertante', onChange: function(object)}} opts
   * @returns {Promise<object|null>} the selected person
   */
  async function init(opts) {
    const kind = opts.kind;
    const host = document.getElementById('persona');
    const isBen = kind === 'beneficiario';
    const path = isBen ? '/api/beneficiarios?ativo=true' : '/api/ofertantes?ativo=true';
    const idKey = isBen ? 'idBeneficiario' : 'idOfertante';
    let list = [];
    try {
      list = await RP.api.get(path);
    } catch (e) {
      host.innerHTML = '<span class="hello">' + RP.esc(e.message) + '</span>';
      return null;
    }
    if (!list.length) {
      host.innerHTML = '<span class="hello">Nenhum cadastro ativo.</span>';
      return null;
    }
    const fromUrl = new URLSearchParams(window.location.search).get(kind);
    const saved = fromUrl || store('rp.' + kind);
    let current = list.find((p) => String(p[idKey]) === String(saved)) || list[0];

    const options = list.map((p) => '<option value="' + p[idKey] + '"' + (p === current ? ' selected' : '') + '>' +
      RP.esc(p.nome) + '</option>').join('');
    host.innerHTML =
      '<span class="avatar ' + (isBen ? 'ben' : '') + '" aria-hidden="true"></span>' +
      '<span class="hello" id="persona-hello"></span>' +
      '<label for="persona-select">Você é:</label>' +
      '<select id="persona-select">' + options + '</select>';
    const sel = host.querySelector('select');
    const avatar = host.querySelector('.avatar');
    const hello = host.querySelector('#persona-hello');

    function apply(p, notify) {
      current = p;
      avatar.textContent = (p.nome || '?').trim().charAt(0).toUpperCase();
      hello.innerHTML = 'Olá, <strong>' + RP.esc(p.nome) + '</strong>';
      store('rp.' + kind, String(p[idKey]));
      setUrlParam(kind, p[idKey]);
      // keep the persona when moving between the ofertante pages
      document.querySelectorAll('a[data-keep-persona]').forEach((a) => {
        const u = new URL(a.getAttribute('href'), window.location.href);
        u.searchParams.set(kind, p[idKey]);
        a.setAttribute('href', u.pathname + u.search);
      });
      if (notify && opts.onChange) opts.onChange(p);
    }
    sel.addEventListener('change', () => {
      const p = list.find((x) => String(x[idKey]) === sel.value);
      if (p) apply(p, true);
    });
    apply(current, false);
    return current;
  }

  window.Persona = { init };
})();
