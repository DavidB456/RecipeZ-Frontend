const API_BASE = 'http://18.225.167.226:8080';

// ─────────────────────────────────────────
// SESSION STATE
// ─────────────────────────────────────────
let session = {
  userId:   null,
  username: null,
  isAdmin: false
};
let myRecipesCache    = [];
let exploreCache      = [];
let selectedRecipe    = null;
let isOwnRecipe       = false;

// ─────────────────────────────────────────
// API HELPERS
// ─────────────────────────────────────────
async function api(method, path, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
  };
  if (body !== undefined) opts.body = JSON.stringify(body);
  const res = await fetch(API_BASE + path, opts);
  const text = await res.text();
  if (!res.ok) {
    let msg = text;
    try { const j = JSON.parse(text); msg = j.message || j.error || text; } catch {}
    throw new Error(msg || `HTTP ${res.status}`);
  }
  if (!text) return null;
  return JSON.parse(text);
}

// ─────────────────────────────────────────
// PAGE ROUTING
// ─────────────────────────────────────────
function showPage(id, tab) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById(id).classList.add('active');
  if (tab) switchAuthTab(tab);
}

// ─────────────────────────────────────────
// AUTH
// ─────────────────────────────────────────
function switchAuthTab(tab) {
  const isLogin = tab === 'login';
  document.getElementById('form-login').style.display     = isLogin ? 'block' : 'none';
  document.getElementById('form-register').style.display  = isLogin ? 'none'  : 'block';
  document.getElementById('tab-login').classList.toggle('active',    isLogin);
  document.getElementById('tab-register').classList.toggle('active', !isLogin);
}

async function handleLogin() {
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;
  const errEl    = document.getElementById('login-error');
  const btn      = document.getElementById('login-btn');
  errEl.textContent = '';

  if (!username || !password) { errEl.textContent = 'Username and password required.'; return; }

  btn.disabled = true; btn.textContent = 'Signing in…';
  try {
    const loginRes = await api('POST', '/login', { username, password });
    const user     = await api('GET', '/users/' + loginRes.id);
    session = { userId: user.id, username: user.username, isAdmin: user.username === 'admin' };
    document.getElementById('nav-username').textContent = user.username;
    showPage('page-dashboard');
    switchView('my-recipes');
    loadMyRecipes();
    toast('Welcome back, ' + user.username + '! 👋', 'success');
  } catch (e) {
    errEl.textContent = friendlyError(e.message);
  } finally {
    btn.disabled = false; btn.textContent = 'Sign In';
  }
}

async function handleRegister() {
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value;
  const weight   = parseFloat(document.getElementById('reg-weight').value);
  const age      = parseInt(document.getElementById('reg-age').value);
  const ft       = parseInt(document.getElementById('reg-height-ft').value);
  const inch     = parseInt(document.getElementById('reg-height-in').value);
  const gender   = document.getElementById('reg-gender').value;
  const diet     = document.getElementById('reg-diet').value;
  const goal     = document.getElementById('reg-goal').value;
  const errEl    = document.getElementById('reg-error');
  const succEl   = document.getElementById('reg-success');
  const btn      = document.getElementById('reg-btn');
  errEl.textContent = ''; succEl.textContent = '';

  if (!username)      { errEl.textContent = 'Username is required.'; return; }
  if (!password)      { errEl.textContent = 'Password is required.'; return; }
  if (isNaN(weight))  { errEl.textContent = 'Enter a valid weight.'; return; }
  if (isNaN(age))     { errEl.textContent = 'Enter a valid age.'; return; }

  const heightDecimal = ft + (inch / 12.0);
  const isMan = gender === 'Male';
  const bmr = isMan
    ? Math.round(88.362 + (13.397 * weight * 0.453592) + (4.799 * heightDecimal * 30.48) - (5.677 * age))
    : Math.round(447.593 + (9.247 * weight * 0.453592) + (3.098 * heightDecimal * 30.48) - (4.330 * age));

  btn.disabled = true; btn.textContent = 'Creating…';
  try {
    await api('POST', '/users', { username, password, bodyGoal: goal, dietType: diet, weight, height: heightDecimal, age, bmr, man: isMan });
    succEl.textContent = '✓ Account created! You can now sign in.';
    setTimeout(() => switchAuthTab('login'), 1500);
  } catch (e) {
    const msg = e.message.toLowerCase();
    errEl.textContent = (msg.includes('unique') || msg.includes('constraint'))
      ? 'That username is already taken.' : friendlyError(e.message);
  } finally {
    btn.disabled = false; btn.textContent = 'Create Account';
  }
}

function handleLogout() {
  session = { userId: null, username: null };
  myRecipesCache = []; exploreCache = []; selectedRecipe = null;
  showPage('page-landing');
  toast('Signed out successfully.');
}

// ─────────────────────────────────────────
// DASHBOARD VIEW SWITCHING
// ─────────────────────────────────────────
function switchView(view) {
  document.querySelectorAll('.center-view').forEach(v => v.classList.remove('active'));
  document.querySelectorAll('.sidebar-btn').forEach(b => b.classList.remove('active'));
  document.getElementById('view-' + view).classList.add('active');
  const map = { 'my-recipes':'sb-home', 'create-recipe':'sb-create', 'explore':'sb-explore' };
  if (map[view]) document.getElementById(map[view]).classList.add('active');
  clearDetail();
}

// ─────────────────────────────────────────
// MY RECIPES
// ─────────────────────────────────────────
async function loadMyRecipes() {
  const grid = document.getElementById('my-recipe-grid');
  grid.innerHTML = '<div class="spinner"></div>';
  try {
    myRecipesCache = await api('GET', '/recipes?userId=' + session.userId) || [];
    filterMyRecipes();
  } catch (e) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>${friendlyError(e.message)}</p></div>`;
  }
}

function filterMyRecipes() {
  const q    = document.getElementById('my-search').value.toLowerCase();
  const diet = document.getElementById('my-diet-filter').value;
  const sort = document.getElementById('my-sort').value;
  let list = [...myRecipesCache];
  if (q)    list = list.filter(r => r.name && r.name.toLowerCase().includes(q));
  if (diet) list = list.filter(r => r.dietType === diet);
  list = applySort(list, sort);
  renderMyGrid(list);
}

function renderMyGrid(recipes) {
  const grid = document.getElementById('my-recipe-grid');
  grid.innerHTML = '';
  // Plus card
  const plus = document.createElement('div');
  plus.className = 'recipe-card plus-card';
  plus.innerHTML = '<div><div class="plus-icon">＋</div><div class="recipe-card-name" style="color:var(--yellow);font-size:13px;margin-top:6px">New Recipe</div></div>';
  plus.onclick = () => switchView('create-recipe');
  grid.appendChild(plus);

  if (!recipes.length) {
    grid.innerHTML += '<div class="empty-state" style="grid-column:2/-1"><div class="empty-icon">🍽️</div><p>No recipes yet — create your first one!</p></div>';
    return;
  }
  recipes.forEach(r => grid.appendChild(makeCard(r, true)));
}

function makeCard(r, isOwn) {
  const card = document.createElement('div');
  card.className = 'recipe-card';
  if (selectedRecipe && selectedRecipe.id === r.id) card.classList.add('selected');
  card.innerHTML = `<div><div class="recipe-card-name">${esc(r.name)}</div><div class="recipe-card-cal">${r.calories != null ? r.calories + ' kcal' : ''}</div></div>`;
  card.onclick = () => selectRecipe(r, isOwn, card);
  return card;
}

function selectRecipe(r, isOwn, cardEl) {
  document.querySelectorAll('.recipe-card').forEach(c => c.classList.remove('selected'));
  cardEl.classList.add('selected');
  selectedRecipe = r;
  isOwnRecipe = isOwn;
  renderDetail(r, isOwn);
}

// ─────────────────────────────────────────
// DETAIL PANEL
// ─────────────────────────────────────────
function renderDetail(r, isOwn) {
  const body = document.getElementById('detail-body');
  const acts = document.getElementById('detail-actions');

  const ings = (r.ingredients || []).map(i =>
    `<li>${i.quantifier} ${i.measurementType} ${esc(i.name)}</li>`).join('');

  body.innerHTML = `
    <div class="detail-name">${esc(r.name)}</div>
    <div class="detail-section">
      <div class="detail-section-title">Diet</div>
      <div><span class="detail-tag">${r.dietType || 'NONE'}</span></div>
    </div>
    ${r.calories != null ? `<div class="detail-section"><div class="detail-section-title">Calories</div><div class="detail-section-body">${r.calories} kcal</div></div>` : ''}
    ${r.description ? `<div class="detail-section"><div class="detail-section-title">Description</div><div class="detail-section-body">${esc(r.description)}</div></div>` : ''}
    ${r.instructions ? `<div class="detail-section"><div class="detail-section-title">Instructions</div><div class="detail-section-body">${esc(r.instructions)}</div></div>` : ''}
    ${ings ? `<div class="detail-section"><div class="detail-section-title">Ingredients</div><ul class="ingredient-list">${ings}</ul></div>` : ''}
  `;
  acts.style.display = (isOwn || session.isAdmin) ? 'block' : 'none';
}

function clearDetail() {
  document.getElementById('detail-body').innerHTML = '<div class="detail-empty">Select a recipe to see details</div>';
  document.getElementById('detail-actions').style.display = 'none';
  selectedRecipe = null;
}

// ─────────────────────────────────────────
// DELETE RECIPE
// ─────────────────────────────────────────
async function handleDeleteRecipe() {
  if (!selectedRecipe || !isOwnRecipe) return;
  if (!confirm(`Delete "${selectedRecipe.name}"?`)) return;
  const btn = document.getElementById('delete-btn');
  btn.disabled = true; btn.textContent = 'Deleting…';
  try {
    await api('DELETE', '/recipes/' + selectedRecipe.id);
    toast('Recipe deleted.', 'success');
    clearDetail();
    loadMyRecipes();
  } catch (e) {
    toast('Delete failed: ' + friendlyError(e.message), 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Delete Recipe';
  }
}

// ─────────────────────────────────────────
// CREATE RECIPE
// ─────────────────────────────────────────
function addIngredientRow() {
  const container = document.getElementById('ingredient-rows');
  const row = document.createElement('div');
  row.className = 'ingredient-row';
  row.innerHTML = `
    <input type="text" placeholder="Ingredient name" class="ing-name" />
    <input type="number" placeholder="Qty" class="ing-qty" step="0.1" />
    <select class="ing-unit">
      <option>NUMBER</option><option>TEASPOON</option><option>TABLESPOON</option>
      <option>CUP</option><option>QUART</option><option>GALLON</option>
      <option>GRAM</option><option>OUNCE</option><option>POUND</option>
    </select>
    <button class="remove-ing-btn" onclick="this.parentElement.remove()">✕</button>
  `;
  container.appendChild(row);
}

async function handleCreateRecipe() {
  const name         = document.getElementById('cr-name').value.trim();
  const description  = document.getElementById('cr-desc').value.trim();
  const instructions = document.getElementById('cr-instructions').value.trim();
  const caloriesStr  = document.getElementById('cr-calories').value;
  const dietType     = document.getElementById('cr-diet').value;
  const errEl        = document.getElementById('cr-error');
  const btn          = document.getElementById('cr-btn');
  errEl.textContent  = '';

  if (!name) { errEl.textContent = 'Recipe title is required.'; return; }
  const calories = parseInt(caloriesStr);
  if (isNaN(calories)) { errEl.textContent = 'Calories must be a number.'; return; }

  const ingredients = [];
  const rows = document.querySelectorAll('.ingredient-row');
  for (const row of rows) {
    const iName = row.querySelector('.ing-name').value.trim();
    const iQty  = parseFloat(row.querySelector('.ing-qty').value);
    const iUnit = row.querySelector('.ing-unit').value;
    if (!iName)     { errEl.textContent = 'All ingredient names are required.'; return; }
    if (isNaN(iQty)){ errEl.textContent = 'All ingredient quantities must be numbers.'; return; }
    ingredients.push({ name: iName, quantifier: iQty, measurementType: iUnit });
  }

  btn.disabled = true; btn.textContent = 'Saving…';
  try {
    await api('POST', '/recipes?userId=' + session.userId, { name, description, instructions, calories, dietType, ingredients });
    toast('Recipe saved! 🎉', 'success');
    // Reset form
    document.getElementById('cr-name').value = '';
    document.getElementById('cr-desc').value = '';
    document.getElementById('cr-instructions').value = '';
    document.getElementById('cr-calories').value = '';
    document.getElementById('ingredient-rows').innerHTML = '';
    switchView('my-recipes');
    loadMyRecipes();
  } catch (e) {
    errEl.textContent = friendlyError(e.message);
  } finally {
    btn.disabled = false; btn.textContent = 'Save Recipe';
  }
}

// ─────────────────────────────────────────
// EXPLORE
// ─────────────────────────────────────────
async function loadExplore() {
  const userId = document.getElementById('explore-user-id').value.trim();
  const grid   = document.getElementById('explore-recipe-grid');
  const stats  = document.getElementById('explore-stats');
  grid.innerHTML = '<div class="spinner"></div>';
  stats.style.display = 'none';

  const path = userId ? `/recipes?userId=${userId}` : '/recipes';
  try {
    exploreCache = await api('GET', path) || [];
    if (userId) {
      const uniqueDiets = [...new Set(exploreCache.map(r => r.dietType).filter(Boolean))];
      const totalCal    = exploreCache.reduce((s, r) => s + (r.calories || 0), 0);
      stats.style.display = 'grid';
      stats.innerHTML = `
        <div class="stat-card"><div class="stat-num">${exploreCache.length}</div><div class="stat-label">Recipes</div></div>
        <div class="stat-card"><div class="stat-num">${uniqueDiets.length}</div><div class="stat-label">Diet Types</div></div>
        <div class="stat-card"><div class="stat-num">${exploreCache.length ? Math.round(totalCal/exploreCache.length) : 0}</div><div class="stat-label">Avg Calories</div></div>
      `;
    }
    filterExplore();
  } catch (e) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>${friendlyError(e.message)}</p></div>`;
  }
}

function clearUserLookup() {
  document.getElementById('explore-user-id').value = '';
  document.getElementById('explore-stats').style.display = 'none';
  exploreCache = [];
  document.getElementById('explore-recipe-grid').innerHTML =
    '<div class="empty-state"><div class="empty-icon">🔍</div><p>Enter a User ID or search to find recipes</p></div>';
}

function filterExplore() {
  const q    = document.getElementById('explore-search').value.toLowerCase();
  const diet = document.getElementById('explore-diet').value;
  const sort = document.getElementById('explore-sort').value;
  let list = [...exploreCache];
  if (q)    list = list.filter(r => r.name && r.name.toLowerCase().includes(q));
  if (diet) list = list.filter(r => r.dietType === diet);
  list = applySort(list, sort);
  renderExploreGrid(list);
}

function renderExploreGrid(recipes) {
  const grid = document.getElementById('explore-recipe-grid');
  grid.innerHTML = '';
  if (!recipes.length) {
    grid.innerHTML = '<div class="empty-state"><div class="empty-icon">🍽️</div><p>No recipes match your filters.</p></div>';
    return;
  }
  recipes.forEach(r => grid.appendChild(makeCard(r, session.isAdmin)));
}

// ─────────────────────────────────────────
// SORT HELPER
// ─────────────────────────────────────────
function applySort(list, sort) {
  if (!sort) return list;
  return [...list].sort((a, b) => {
    if (sort === 'name-asc')  return (a.name||'').localeCompare(b.name||'');
    if (sort === 'name-desc') return (b.name||'').localeCompare(a.name||'');
    if (sort === 'cal-asc')   return (a.calories||0) - (b.calories||0);
    if (sort === 'cal-desc')  return (b.calories||0) - (a.calories||0);
    return 0;
  });
}

// ─────────────────────────────────────────
// UTILITIES
// ─────────────────────────────────────────
function esc(s) {
  if (!s) return '';
  return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function friendlyError(msg) {
  if (!msg) return 'An unexpected error occurred.';
  const m = msg.toLowerCase();
  if (m.includes('401') || m.includes('unauthorized') || m.includes('invalid credentials')) return 'Incorrect username or password.';
  if (m.includes('404') || m.includes('not found')) return 'Not found.';
  if (m.includes('unique') || m.includes('constraint')) return 'That username is already taken.';
  if (m.includes('network') || m.includes('failed to fetch')) return 'Cannot reach the server. Is the backend running?';
  return msg;
}

let toastTimer;
function toast(msg, type) {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = 'show' + (type ? ' ' + type : '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = ''; }, 3200);
}

// Init first ingredient row
addIngredientRow();