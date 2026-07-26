/**
 * Nova — Client-side interactivity.
 * Chart rendering, theme toggle, persistent preferences.
 */
(function () {
  'use strict';

  /* ── Theme toggle ─────────────────────────────────── */
  function initTheme() {
    var saved = localStorage.getItem('nova-theme');
    if (saved === 'dark') {
      document.documentElement.classList.add('dark');
    } else if (saved === 'light') {
      document.documentElement.classList.remove('dark');
    }
    updateToggleLabel();
  }

  function updateToggleLabel() {
    var btn = document.getElementById('theme-toggle');
    if (!btn) return;
    var isDark = document.documentElement.classList.contains('dark');
    btn.textContent = isDark ? 'Switch to Light Mode' : 'Toggle Dark Mode';
  }

  function toggleTheme() {
    var html = document.documentElement;
    var isDark = html.classList.contains('dark');
    if (isDark) {
      html.classList.remove('dark');
      localStorage.setItem('nova-theme', 'light');
    } else {
      html.classList.add('dark');
      localStorage.setItem('nova-theme', 'dark');
    }
    updateToggleLabel();
    redrawCharts();
  }

  var themeBtn = document.getElementById('theme-toggle');
  if (themeBtn) {
    themeBtn.addEventListener('click', toggleTheme);
  }

  /* ── Chart colour helpers ─────────────────────────── */
  function getCSSVar(name) {
    return getComputedStyle(document.documentElement)
      .getPropertyValue(name).trim();
  }

  function chartAccent() {
    return getCSSVar('--accent') || '#0074ca';
  }

  function chartMuted() {
    return getCSSVar('--muted-foreground') || '#5a656e';
  }

  function chartGridColor() {
    var isDark = document.documentElement.classList.contains('dark');
    return isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';
  }

  function chartTextColor() {
    return getCSSVar('--muted-foreground') || '#5a656e';
  }

  /* ── Chart instances ──────────────────────────────── */
  var chartInstances = {};

  function destroyChart(key) {
    if (chartInstances[key]) {
      chartInstances[key].destroy();
      delete chartInstances[key];
    }
  }

  function redrawCharts() {
    initDashboardChart();
    initDeviceChart();
  }

  /* ── Dashboard chart ──────────────────────────────── */
  function initDashboardChart() {
    var dataEl = document.getElementById('dashboard-chart-data');
    var canvas = document.getElementById('dashboard-chart');
    if (!dataEl || !canvas) return;

    destroyChart('dashboard');

    var raw;
    try { raw = JSON.parse(dataEl.textContent); } catch (e) { return; }

    var accent = chartAccent();
    var labels = raw.labels.map(function (ts) {
      try {
        var d = new Date(ts);
        return d.getHours().toString().padStart(2, '0') + ':00';
      } catch (e) { return ts; }
    });

    chartInstances.dashboard = new Chart(canvas, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: raw.datasets[0].label,
          data: raw.datasets[0].data,
          borderColor: accent,
          backgroundColor: accent + '15',
          borderWidth: 2,
          pointRadius: 2,
          pointHoverRadius: 5,
          fill: true,
          tension: 0.35
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { intersect: false, mode: 'index' },
        plugins: {
          legend: { display: true, labels: { color: chartTextColor(), usePointStyle: true, padding: 20 } },
          tooltip: { backgroundColor: getCSSVar('--card') || '#fff', titleColor: getCSSVar('--foreground') || '#000', bodyColor: getCSSVar('--muted-foreground') || '#666', borderColor: getCSSVar('--border') || '#ddd', borderWidth: 1 }
        },
        scales: {
          x: { grid: { color: chartGridColor() }, ticks: { color: chartTextColor(), maxTicksLimit: 12 } },
          y: { grid: { color: chartGridColor() }, ticks: { color: chartTextColor() }, beginAtZero: false }
        }
      }
    });
  }

  /* ── Device history chart ─────────────────────────── */
  function initDeviceChart() {
    var dataEl = document.getElementById('device-chart-data');
    var canvas = document.getElementById('device-history-chart');
    if (!dataEl || !canvas) return;

    destroyChart('device');

    var raw;
    try { raw = JSON.parse(dataEl.textContent); } catch (e) { return; }

    var accent = chartAccent();
    var labels = raw.labels.map(function (ts) {
      try {
        var d = new Date(ts);
        return d.getHours().toString().padStart(2, '0') + ':00';
      } catch (e) { return ts; }
    });

    chartInstances.device = new Chart(canvas, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: raw.datasets[0].label,
          data: raw.datasets[0].data,
          borderColor: accent,
          backgroundColor: accent + '15',
          borderWidth: 2,
          pointRadius: 2,
          pointHoverRadius: 5,
          fill: true,
          tension: 0.35
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { intersect: false, mode: 'index' },
        plugins: {
          legend: { display: true, labels: { color: chartTextColor(), usePointStyle: true, padding: 20 } },
          tooltip: { backgroundColor: getCSSVar('--card') || '#fff', titleColor: getCSSVar('--foreground') || '#000', bodyColor: getCSSVar('--muted-foreground') || '#666', borderColor: getCSSVar('--border') || '#ddd', borderWidth: 1 }
        },
        scales: {
          x: { grid: { color: chartGridColor() }, ticks: { color: chartTextColor(), maxTicksLimit: 12 } },
          y: { grid: { color: chartGridColor() }, ticks: { color: chartTextColor() }, beginAtZero: false }
        }
      }
    });
  }

  /* ── Boot ─────────────────────────────────────────── */
  initTheme();

  if (typeof Chart !== 'undefined') {
    initDashboardChart();
    initDeviceChart();
  } else {
    /* Chart.js may load after this script; retry once */
    window.addEventListener('load', function () {
      if (typeof Chart !== 'undefined') {
        initDashboardChart();
        initDeviceChart();
      }
    });
  }
})();
