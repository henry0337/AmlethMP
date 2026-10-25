/* Shared design scripts.
   - Screens: ?state=loading | error sets data-state on <html> so a screen can show that state.
   - Overview (index.html): screen list, device presets and state buttons for the live preview. */

(function () {
  var params = new URLSearchParams(location.search);
  var state = params.get('state');
  if (state) document.documentElement.dataset.state = state;

  var frame = document.getElementById('frame');
  if (!frame) return;

  var stage = document.getElementById('stage');
  var wrap = document.getElementById('frameWrap');
  var sizeLabel = document.getElementById('sizeLabel');
  var current = { src: null, state: '', w: 390, h: 844 };

  function fit() {
    var pad = 32;
    var scale = Math.min(1, (stage.clientWidth - pad) / current.w, (stage.clientHeight - pad) / current.h);
    wrap.style.width = current.w * scale + 'px';
    wrap.style.height = current.h * scale + 'px';
    frame.style.width = current.w + 'px';
    frame.style.height = current.h + 'px';
    frame.style.transform = 'scale(' + scale + ')';
    sizeLabel.textContent = current.w + ' × ' + current.h + (scale < 1 ? ' · ' + Math.round(scale * 100) + '%' : '');
  }

  function load() {
    if (!current.src) return;
    frame.src = current.src + (current.state ? '?state=' + current.state : '');
  }

  function activate(group, el) {
    document.querySelectorAll(group).forEach(function (node) {
      node.setAttribute('aria-pressed', node === el ? 'true' : 'false');
      node.classList.toggle('is-active', node === el);
    });
  }

  document.querySelectorAll('.screen').forEach(function (el) {
    el.addEventListener('click', function () {
      current.src = el.dataset.src;
      activate('.screen', el);
      load();
    });
  });

  // Mobile / Tablet / Desktop dropdowns: picking a size in one resets the other two.
  var sizeSelects = document.querySelectorAll('.preset-select');
  sizeSelects.forEach(function (select) {
    select.addEventListener('change', function () {
      var size = select.value.split('x');
      current.w = Number(size[0]);
      current.h = Number(size[1]);
      sizeSelects.forEach(function (other) {
        if (other !== select) other.selectedIndex = 0;
        other.classList.toggle('is-active', other === select);
      });
      fit();
    });
  });

  document.querySelectorAll('.state').forEach(function (el) {
    el.addEventListener('click', function () {
      current.state = el.dataset.state;
      activate('.state', el);
      load();
    });
  });

  window.addEventListener('resize', fit);

  document.querySelector('.screen').click();
  sizeSelects[0].value = '440x956';
  sizeSelects[0].dispatchEvent(new Event('change'));
})();
