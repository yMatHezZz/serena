import { renderPublic } from './public.js';
import { renderAccount } from './account.js';
const $ = (selector, root = document) => root.querySelector(selector);
const app = $('#app'), editor = $('#editor'), confirmation = $('#confirmation');
const paths = {
  home:'M3 10 12 3l9 7v10H3Z M9 20v-7h6v7', calendar:'M4 5h16v16H4Z M4 10h16 M8 3v4 M16 3v4 M8 14h2 M14 14h2 M8 18h2',
  users:'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8 M22 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75',
  heart:'M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z',
  professional:'M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8 M4 21v-2a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v2 M12 16v5 M9.5 18.5h5',
  leaf:'M20 3C8 2 2 8 5 15c3 8 16 5 15-12Z M5 20 16 9 M9 16v-5 M13 12h5', clock:'M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18 M12 7v5l3 2',
  plus:'M12 5v14 M5 12h14', arrow:'M5 12h14 M13 6l6 6-6 6', search:'M10.5 3a7.5 7.5 0 1 0 0 15 7.5 7.5 0 0 0 0-15 M16 16l5 5',
  edit:'m16 3 5 5-12 12H4v-5Z M13 6l5 5', trash:'M3 6h18 M9 6V3h6v3 M5 6l1 15h12l1-15 M10 10v7 M14 10v7',
  eye:'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6',
  close:'M6 6l12 12 M18 6 6 18', logout:'M9 3H4v18h5 M10 12h11 M17 8l4 4-4 4', shield:'m12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6Z M8 12l3 3 5-6',
  refresh:'M20 7a9 9 0 1 0 1 8 M20 3v5h-5', chevron:'m9 5 7 7-7 7', menu:'M4 6h16 M4 12h16 M4 18h16', check:'m5 12 4 4L19 6'
};
const icon = (name) => `<svg class="icon" aria-hidden="true" viewBox="0 0 24 24"><path d="${paths[name] || paths.leaf}"/></svg>`;
const brand = `<div class="brand"><svg class="brand-mark" aria-hidden="true" viewBox="0 0 40 40"><path d="M12 34v-6C4 23 6 10 13 6c11-6 22 1 21 12l3 7h-5v9h-9l-5-6"/><path class="brand-brain" d="M16 11c-5-3-8 5-4 7-3 5 4 8 7 4 4 2 8-3 5-6 3-5-3-9-6-5m0 0v12m-5-8 5 3 5-4"/></svg>Serena</div>`;
const esc = (value) => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const labels = {ACTIVE:'Ativo',INACTIVE:'Inativo',SCHEDULED:'Agendada',CANCELLED:'Cancelada',COMPLETED:'Concluída',true:'Disponível',false:'Indisponível'};
const field = (name, label, type = 'text', required = false, extra = {}) => ({name,label,type,required,...extra});
const resources = {
  consultations:{title:'Consultas',singular:'consulta',icon:'calendar',description:'Organize os encontros e acompanhe cada agendamento.',fields:[
    field('patientId','Paciente','ref',true,{ref:'patients',immutable:true}),field('professionalId','Profissional','ref',true,{ref:'professionals',immutable:true}),
    field('scheduledStartAt','Início da consulta','datetime-local',true),field('scheduledEndAt','Término da consulta','datetime-local',true),
    field('status','Status','select',true,{options:['SCHEDULED','CANCELLED','COMPLETED'],default:'SCHEDULED',wide:true})]},
  patients:{title:'Pacientes',singular:'paciente',icon:'heart',description:'Dados dos pacientes cadastrados.',fields:[
    field('userId','Usuário vinculado','ref',true,{ref:'users',immutable:true,wide:true}),field('dateOfBirth','Data de nascimento','date',true),
    field('gender','Gênero','select',false,{options:[['NAO_INFORMADO','Não informado'],['FEMININO','Feminino'],['MASCULINO','Masculino'],['OUTRO','Outro']]}),
    field('phone','Telefone','tel',false,{max:20}),field('address','Endereço','textarea',false,{max:1000,wide:true})]},
  professionals:{title:'Profissionais',singular:'profissional',icon:'professional',description:'Dados dos profissionais cadastrados.',fields:[
    field('userId','Usuário vinculado','ref',true,{ref:'users',immutable:true,wide:true}),field('licenseNumber','Registro profissional (CRP)','text',true,{max:50}),
    field('specialty','Especialidade','text',true,{max:100}),field('phone','Telefone','tel',false,{max:20}),field('bio','Sobre o profissional','textarea',false,{max:5000,wide:true})]},
  'availability-slots':{title:'Disponibilidades',singular:'disponibilidade',icon:'clock',description:'Defina os horários de atendimento dos profissionais.',fields:[
    field('professionalId','Profissional','ref',true,{ref:'professionals',immutable:true,wide:true}),field('startAt','Início do horário','datetime-local',true),
    field('endAt','Término do horário','datetime-local',true),field('available','Disponibilidade','boolean',true,{default:true,wide:true})]},
  activities:{title:'Atividades',singular:'atividade',icon:'leaf',description:'Cadastre e edite as atividades.',fields:[
    field('name','Nome da atividade','text',true,{max:100,wide:true}),field('activityType','Tipo de atividade','text',true,{max:50,help:'Ex.: BREATHING, REFLECTION ou MINDFULNESS.'}),
    field('active','Status','boolean',true,{default:true}),field('description','Descrição','textarea',false,{max:5000,wide:true}),
    field('configuration','Configuração da atividade','json',true,{default:{},wide:true,help:'Objeto JSON válido, com até 16 KiB. Ex.: {"seconds": 30}.'})]},
  users:{title:'Usuários',singular:'usuário',icon:'users',description:'Cadastre as pessoas antes de criar seus perfis.',fields:[
    field('name','Nome completo','text',true,{max:100,wide:true}),field('email','E-mail','email',true,{max:100}),
    field('status','Status','select',true,{options:['ACTIVE','INACTIVE'],default:'ACTIVE'}),field('password','Senha do cadastro','password',true,{min:12,max:72,wide:true,help:'Mínimo de 12 caracteres; até 72 bytes UTF-8. Não concede acesso administrativo.'})]}
};
const state = {token:null,username:'',ready:false,route:'dashboard',data:{},query:'',filter:'',date:'',page:0,busy:false,error:'',generation:0};
let toastTimer;
const dateFormat = new Intl.DateTimeFormat('pt-BR',{day:'2-digit',month:'short',year:'numeric'});
const timeFormat = new Intl.DateTimeFormat('pt-BR',{hour:'2-digit',minute:'2-digit'});
const dateTime = value => value ? `${dateFormat.format(new Date(value))} · ${timeFormat.format(new Date(value))}` : '—';
const localInput = value => {const d=new Date(value);return new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,16);};
const initials = name => String(name || 'S').split(/\s+/).filter(Boolean).slice(0,2).map(n=>n[0]).join('').toUpperCase();
const badge = (value, custom) => `<span class="badge ${['INACTIVE','CANCELLED',false].includes(value)?'gray':value==='COMPLETED'?'blue':''}">${esc(custom || labels[value] || value)}</span>`;
function toast(message) {clearTimeout(toastTimer);$('#toast').textContent=message;$('#toast').classList.add('visible');toastTimer=setTimeout(()=>$('#toast').classList.remove('visible'),4500);}
function logout(message='Você saiu do painel.') {state.generation++;state.token=null;state.ready=false;state.data={};state.username='';state.identity=null;state.error='';editor.close();confirmation.close();location.hash='login';renderLogin();toast(message);}
async function api(path, {method='GET',body} = {}) {
  let response;
  try {response=await fetch(`/api/${path}`,{method,cache:'no-store',credentials:'omit',signal:AbortSignal.timeout(20000),headers:{...(state.token?{Authorization:`Basic ${state.token}`}:{ }),'X-Requested-With':'XMLHttpRequest','X-Serene-Client':'admin-web',...(body!==undefined?{'Content-Type':'application/json'}:{})},...(body!==undefined?{body:JSON.stringify(body)}:{})});}
  catch {throw new Error('Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.');}
  const result=response.status===204?null:await response.json().catch(()=>null);
  if (!response.ok) {
    if(response.status===401&&state.ready) logout('Seu acesso expirou. Entre novamente.');
    const error=new Error(response.status===401?'Usuário ou senha incorretos.':result?.message || `Não foi possível concluir a operação (${response.status}).`);
    error.fields=result?.errors || {};throw error;
  }
  return result;
}
async function all(resource) {
  const rows=[];let page=0;
  do {const result=await api(`${resource}?page=${page}&size=100`);rows.push(...result.content);page++;if(page>=result.totalPages)return rows;}while(page<100);
  throw new Error('Há muitos registros para carregar neste painel. Solicite paginação de servidor para este volume.');
}
async function refresh() {
  const generation=state.generation;state.busy=true;state.error='';renderMain();
  try {const entries=await Promise.all(Object.keys(resources).map(async key=>[key,await all(key)]));if(generation!==state.generation)return false;state.data=Object.fromEntries(entries);return true;}
  catch(error) {if(generation===state.generation)state.error=error.message;return false;}
  finally {if(generation===state.generation){state.busy=false;if(state.ready)renderMain();}}
}
function renderLogin() {
  renderPublic({mount:app,brand,icon,esc,api,login:loginAccount});
}
async function loginAccount(username,password) {
  const generation=++state.generation;
  state.username=username;
  state.token=btoa(Array.from(new TextEncoder().encode(`${username}:${password}`),b=>String.fromCharCode(b)).join(''));
  try {
    const identity=await api('auth/me');
    if(generation!==state.generation)return;
    state.identity=identity;state.username=identity.name;state.ready=true;
    if(identity.role==='ADMIN'){state.route='dashboard';location.hash='dashboard';renderShell();await refresh();}
    else{location.hash='account';await renderAccount({mount:app,brand,icon,esc,api,logout:()=>logout(),current:()=>state.generation===generation&&state.ready});}
  } catch(error){if(generation===state.generation){state.token=null;state.ready=false;}throw error;}
}
function validRoute(){const key=location.hash.slice(1);return key==='dashboard'||resources[key]?key:'dashboard';}
function navigate(route) {if(!resources[route]&&route!=='dashboard')return;state.route=route;state.query='';state.filter='';state.date='';state.page=0;location.hash=route;renderShell();renderMain();}
function renderShell() {
  app.innerHTML=`<div class="shell"><div class="nav-scrim"></div><aside class="sidebar" aria-label="Menu principal">${brand}<nav><p class="nav-label">Meu espaço</p><button class="nav-link ${state.route==='dashboard'?'active':''}" data-route="dashboard" ${state.route==='dashboard'?'aria-current="page"':''}>${icon('home')}Visão geral</button>${['consultations','availability-slots'].map(navButton).join('')}<p class="nav-label">Pessoas e cuidado</p>${['patients','professionals','activities'].map(navButton).join('')}<p class="nav-label">Administração</p>${navButton('users')}</nav><div class="sidebar-foot"><div class="demo-note"><strong>Um espaço de demonstração</strong>Trabalhe apenas com dados fictícios neste projeto acadêmico.</div><button class="nav-link" id="logout">${icon('logout')}Sair do painel</button></div></aside><div class="main-wrap"><header class="topbar"><button class="icon-btn mobile-menu" aria-label="Abrir menu" aria-expanded="false">${icon('menu')}</button><div class="breadcrumb"><span>Meu espaço</span><span>/</span><strong>${esc(state.route==='dashboard'?'Visão geral':resources[state.route].title)}</strong></div><button class="icon-btn" id="refresh" aria-label="Atualizar dados">${icon('refresh')}</button><div class="account"><span class="avatar">${esc(initials(state.username))}</span><div><strong>${esc(state.username)}</strong><small>Administração</small></div></div></header><main class="main-content" id="main" tabindex="-1"></main></div></div>`;
  $$('[data-route]').forEach(b=>b.onclick=()=>navigate(b.dataset.route));$('#logout').onclick=()=>logout();$('#refresh').onclick=refresh;
  $('.mobile-menu').onclick=()=>toggleMenu();$('.nav-scrim').onclick=()=>toggleMenu(false);
}
function $$(selector,root=document){return [...root.querySelectorAll(selector)];}
function navButton(key){return `<button class="nav-link ${state.route===key?'active':''}" data-route="${key}" ${state.route===key?'aria-current="page"':''}>${icon(resources[key].icon)}${resources[key].title}</button>`;}
function toggleMenu(force){const open=force ?? !$('.sidebar').classList.contains('open');$('.sidebar').classList.toggle('open',open);$('.nav-scrim').classList.toggle('visible',open);$('.mobile-menu').setAttribute('aria-expanded',String(open));}
function userOf(profile){return (state.data.users||[]).find(u=>u.id===profile?.userId);}
function recordName(key,id){const row=(state.data[key]||[]).find(r=>r.id===id);if(!row)return 'Cadastro não encontrado';return key==='users'?row.name:key==='patients'||key==='professionals'?(userOf(row)?.name || 'Usuário não encontrado'):row.name || id;}
function person(key,id,secondary=''){const name=recordName(key,id);return `<div class="person-cell"><span class="avatar">${esc(initials(name))}</span><div><strong>${esc(name)}</strong>${secondary?`<small>${esc(secondary)}</small>`:''}</div></div>`;}
function renderMain() {
  if(!state.ready||!$('#main'))return;
  const main=$('#main');document.title=`${state.route==='dashboard'?'Visão geral':resources[state.route].title} · Serena`;
  if(state.busy){main.innerHTML='<div class="loading" role="status"><span class="spinner"></span><p>Carregando dados…</p></div>';return;}
  if(state.error){main.innerHTML=`<div class="empty"><h1>Não foi possível carregar os dados.</h1><p>${esc(state.error)}</p><button class="btn primary" id="retry">${icon('refresh')}Tentar novamente</button></div>`;$('#retry').onclick=refresh;return;}
  if(state.route==='dashboard')renderDashboard(main);else renderResource(main);
}
function footer(){return `<footer class="section-footer"><span>Serena · Administração</span><span><span class="dot"></span>Dados carregados da API · ${esc(new Date().toLocaleDateString('pt-BR'))}</span></footer>`;}
function renderDashboard(main) {
  const data=state.data,scheduled=(data.consultations||[]).filter(c=>c.status==='SCHEDULED');
  const upcoming=scheduled.filter(c=>new Date(c.scheduledEndAt)>=new Date()).sort((a,b)=>new Date(a.scheduledStartAt)-new Date(b.scheduledStartAt));
  const today=new Date().toLocaleDateString('pt-BR');const todayCount=scheduled.filter(c=>new Date(c.scheduledStartAt).toLocaleDateString('pt-BR')===today).length;
  const cards=[['Consultas hoje',todayCount,'consultations','calendar','Encontros agendados para hoje'],['Pacientes',(data.patients||[]).length,'patients','heart','Pessoas com perfil cadastrado'],['Profissionais',(data.professionals||[]).length,'professionals','professional','Sua rede de cuidado'],['Atividades ativas',(data.activities||[]).filter(a=>a.active).length,'activities','leaf','Propostas no catálogo']];
  main.innerHTML=`<div class="page-heading"><div><span class="eyebrow">Administração</span><h1>Visão geral</h1><p>Resumo dos cadastros e agendamentos.</p></div><span class="date-label">${esc(new Date().toLocaleDateString('pt-BR',{weekday:'long',day:'numeric',month:'long'}))}</span></div><section class="welcome-banner"><div><h2>Agenda e cadastros</h2><p>Gerencie pacientes, profissionais e horários disponíveis.</p></div><div class="banner-ornament" aria-hidden="true"></div></section><section class="stats" aria-label="Resumo do espaço">${cards.map(([label,count,route,i,note])=>`<button class="stat" data-route="${route}"><div class="stat-head">${label}${icon(i)}</div><div class="stat-number">${count}</div><div class="stat-note">${note}</div></button>`).join('')}</section><div class="dashboard-grid"><section class="panel"><div class="panel-heading"><div><h2>Próximos encontros</h2><p>${upcoming.length} ${upcoming.length===1?'consulta no horizonte':'consultas no horizonte'}</p></div><button class="btn small quiet" data-route="consultations">Ver agenda ${icon('arrow')}</button></div>${upcoming.length?upcoming.slice(0,5).map(c=>`<div class="upcoming-item"><div class="date-tile"><strong>${new Date(c.scheduledStartAt).getDate()}</strong>${esc(new Date(c.scheduledStartAt).toLocaleDateString('pt-BR',{month:'short'}))}</div><div class="upcoming-info"><strong>${esc(recordName('patients',c.patientId))}</strong><p>${esc(recordName('professionals',c.professionalId))}</p></div><div class="time-text">${timeFormat.format(new Date(c.scheduledStartAt))} – ${timeFormat.format(new Date(c.scheduledEndAt))}<br>${badge(c.status)}</div><button class="icon-btn" data-view="${c.id}" aria-label="Ver consulta de ${esc(recordName('patients',c.patientId))}">${icon('chevron')}</button></div>`).join(''):`<div class="empty">${icon('calendar')}<h2>Nenhuma consulta agendada.</h2><p>Crie uma disponibilidade e agende o primeiro encontro.</p><button class="btn primary" data-create="consultations">${icon('plus')}Agendar consulta</button></div>`}</section><aside class="dashboard-aside"><section class="panel"><div class="panel-heading"><h2>Ações rápidas</h2></div><div class="quick-actions">${[['consultations','Agendar consulta','Escolher paciente, profissional e horário'],['patients','Cadastrar paciente','Vincular um perfil a um usuário'],['availability-slots','Abrir disponibilidade','Definir horários de atendimento']].map(([key,label,desc])=>`<button class="quick-action" data-create="${key}"><span class="icon-bubble">${icon(resources[key].icon)}</span><div><strong>${label}</strong><small>${desc}</small></div>${icon('arrow')}</button>`).join('')}</div></section><div class="scope-note"><strong>Limites da demonstração</strong>Este painel gerencia cadastros e agendamentos. Videoconferência e execução de exercícios em sessão ainda não estão disponíveis.</div></aside></div>${footer()}`;
  $$('[data-route]',main).forEach(b=>b.onclick=()=>navigate(b.dataset.route));$$('[data-create]',main).forEach(b=>b.onclick=()=>openEditor(b.dataset.create));$$('[data-view]',main).forEach(b=>b.onclick=()=>openDetails('consultations',b.dataset.view));
}
function renderResource(main) {
  const key=state.route,meta=resources[key];
  main.innerHTML=`<div class="page-heading"><div><span class="eyebrow">${['patients','professionals','activities'].includes(key)?'Pessoas e cuidado':'Meu espaço'}</span><h1>${meta.title}</h1><p>${meta.description}</p></div><div class="heading-actions"><button class="btn primary" id="create">${icon('plus')}${key==='consultations'?'Agendar consulta':`Cadastrar ${meta.singular}`}</button></div></div>${key==='activities'?'<div class="info-strip">Catálogo de atividades. A execução de exercícios durante consultas ainda não está integrada.</div>':''}<div class="toolbar"><div class="search">${icon('search')}<input type="search" id="search" aria-label="Buscar em ${meta.title}" placeholder="Buscar ${meta.title.toLowerCase()}…" value="${esc(state.query)}"></div>${['users','consultations','availability-slots','activities'].includes(key)?`<select id="status-filter" class="filter-select" aria-label="Filtrar por status"><option value="">Todos os status</option>${(key==='users'?['ACTIVE','INACTIVE']:key==='consultations'?['SCHEDULED','CANCELLED','COMPLETED']:['true','false']).map(v=>`<option value="${v}" ${state.filter===v?'selected':''}>${key==='activities'?(v==='true'?'Ativas':'Inativas'):labels[v]}</option>`).join('')}</select>`:''}${key==='consultations'?`<input id="date-filter" class="filter-select" type="date" aria-label="Filtrar por data" value="${esc(state.date)}">`:''}<span class="record-count" id="record-count"></span></div><div id="records"></div>${footer()}`;
  $('#create').onclick=()=>openEditor(key);$('#search').oninput=e=>{state.query=e.target.value;state.page=0;renderRecords();};
  if($('#status-filter'))$('#status-filter').onchange=e=>{state.filter=e.target.value;state.page=0;renderRecords();};
  if($('#date-filter'))$('#date-filter').onchange=e=>{state.date=e.target.value;state.page=0;renderRecords();};renderRecords();
}
function searchable(key,row){return [...Object.values(row).filter(v=>typeof v==='string'),row.userId?recordName('users',row.userId):'',row.patientId?recordName('patients',row.patientId):'',row.professionalId?recordName('professionals',row.professionalId):''].join(' ').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();}
function filteredRows(){const key=state.route,q=state.query.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();return (state.data[key]||[]).filter(row=>searchable(key,row).includes(q)&&(!state.filter||String(row.status??row.available??row.active)===state.filter)&&(!state.date||localInput(row.scheduledStartAt).slice(0,10)===state.date)).sort((a,b)=>key==='consultations'||key==='availability-slots'?new Date(a.scheduledStartAt||a.startAt)-new Date(b.scheduledStartAt||b.startAt):0);}
function actions(key,row){const name=key==='users'||key==='activities'?row.name:key==='consultations'?'consulta':key==='availability-slots'?'disponibilidade':recordName('users',row.userId);return `<div class="row-actions"><button class="icon-btn" data-action="view" data-id="${row.id}" aria-label="Ver ${esc(name)}">${icon('eye')}</button>${key==='consultations'&&row.status!=='SCHEDULED'?'':`<button class="icon-btn" data-action="edit" data-id="${row.id}" aria-label="Editar ${esc(name)}">${icon('edit')}</button>`}<button class="icon-btn delete" data-action="delete" data-id="${row.id}" aria-label="Excluir ${esc(name)}">${icon('trash')}</button></div>`;}
function renderRecords() {
  const key=state.route,rows=filteredRows(),total=rows.length;state.page=Math.min(state.page,Math.max(0,Math.ceil(total/10)-1));const visible=rows.slice(state.page*10,state.page*10+10);$('#record-count').textContent=`${total} ${total===1?'registro':'registros'}`;
  let html;
  if(!total){html=`<div class="panel empty">${icon(resources[key].icon)}<h2>${state.query||state.filter||state.date?'Nenhum resultado para os filtros.':'Nenhum cadastro encontrado.'}</h2><p>${state.query||state.filter||state.date?'Ajuste a busca ou limpe os filtros para ver outros registros.':`Cadastre ${resources[key].singular==='consulta'?'uma consulta':`um novo registro de ${resources[key].singular}`} para começar.`}</p><button class="btn" id="empty-action">${state.query||state.filter||state.date?'Limpar filtros':'Criar primeiro registro'}</button></div>`;}
  else if(key==='activities'){html=`<div class="activity-grid">${visible.map(row=>`<article class="activity-card"><div class="activity-art">${icon('leaf')}</div><div class="activity-body">${badge(row.active,row.active?'Ativa':'Inativa')}<h2>${esc(row.name)}</h2><p>${esc(row.description||'Sem descrição cadastrada.')}</p><div class="activity-bottom"><small>${esc(row.activityType)}</small>${actions(key,row)}</div></div></article>`).join('')}</div>${pagination(total)}`;}
  else {const headers={users:['Pessoa','Status','Criado em'],patients:['Paciente','Nascimento','Telefone'],professionals:['Profissional','Especialidade','Registro'],consultations:['Paciente','Profissional','Data e horário','Status'],'availability-slots':['Profissional','Início','Término','Status']}[key];
    html=`<section class="panel"><div class="table-scroll"><table><caption class="skip-link">Lista de ${esc(resources[key].title.toLowerCase())}</caption><thead><tr>${headers.map(h=>`<th scope="col">${h}</th>`).join('')}<th scope="col" style="text-align:right">Ações</th></tr></thead><tbody>${visible.map(row=>`<tr>${cells(key,row).map(c=>`<td>${c}</td>`).join('')}<td>${actions(key,row)}</td></tr>`).join('')}</tbody></table></div>${pagination(total)}</section>`;}
  $('#records').innerHTML=html;
  if($('#empty-action'))$('#empty-action').onclick=()=>{if(state.query||state.filter||state.date){state.query='';state.filter='';state.date='';renderResource($('#main'));}else openEditor(key);};
  $$('[data-action]',$('#records')).forEach(b=>b.onclick=()=>b.dataset.action==='view'?openDetails(key,b.dataset.id):b.dataset.action==='edit'?openEditor(key,b.dataset.id):confirmDelete(key,b.dataset.id));
  if($('#prev'))$('#prev').onclick=()=>{state.page--;renderRecords();};if($('#next'))$('#next').onclick=()=>{state.page++;renderRecords();};
}
function cells(key,row){switch(key){case 'users':return [person('users',row.id,row.email),badge(row.status),esc(dateFormat.format(new Date(row.createdAt)))];case 'patients':return [person('users',row.userId,userOf(row)?.email),esc(new Date(row.dateOfBirth+'T12:00:00').toLocaleDateString('pt-BR')),esc(row.phone||'Não informado')];case 'professionals':return [person('users',row.userId,userOf(row)?.email),esc(row.specialty),esc(row.licenseNumber)];case 'consultations':return [person('patients',row.patientId),esc(recordName('professionals',row.professionalId)),esc(dateTime(row.scheduledStartAt)),badge(row.status)];default:return [person('professionals',row.professionalId),esc(dateTime(row.startAt)),esc(dateTime(row.endAt)),badge(row.available)];}}
function pagination(total){return `<div class="pagination"><span>${state.page*10+1}–${Math.min((state.page+1)*10,total)} de ${total} registros</span><div><button class="btn small" id="prev" ${state.page===0?'disabled':''}>Anterior</button><button class="btn small" id="next" ${(state.page+1)*10>=total?'disabled':''}>Próxima</button></div></div>`;}
function optionsFor(f,key,id){if(f.type==='boolean')return [['true',key==='activities'?'Ativa':'Disponível'],['false',key==='activities'?'Inativa':'Indisponível']];if(f.type==='ref'){let rows=state.data[f.ref]||[];if(f.ref==='users'&&!id){const used=new Set((state.data[key]||[]).map(r=>r.userId));rows=rows.filter(u=>!used.has(u.id));}return rows.map(r=>[r.id,recordName(f.ref,r.id)+(f.ref==='users'?` · ${r.email}`:'')]);}return (f.options||[]).map(v=>Array.isArray(v)?v:[v,labels[v]||v]);}
function formField(f,key,row){const editing=!!row,required=f.name==='password'? !editing:f.required;let value=editing?row[f.name]:f.default??'';if(f.type==='datetime-local'&&value)value=localInput(value);if(f.type==='json')value=JSON.stringify(value??{},null,2);const locked=editing&&f.immutable,attrs=`id="field-${f.name}" name="${f.name}" ${required?'required':''} ${locked?'disabled':''} ${f.max?`maxlength="${f.max}"`:''} ${f.min&&required?`minlength="${f.min}"`:''} aria-describedby="help-${f.name} error-${f.name}"`;let input;
  if(['select','ref','boolean'].includes(f.type)){input=`<select ${attrs}><option value="">Selecione…</option>${optionsFor(f,key,row?.id).map(([v,label])=>`<option value="${esc(v)}" ${String(value??'')===String(v)?'selected':''}>${esc(label)}</option>`).join('')}</select>`;}
  else if(['textarea','json'].includes(f.type))input=`<textarea ${attrs} ${f.type==='json'?'spellcheck="false" rows="4"':''}>${esc(value)}</textarea>`;
  else input=`<input ${attrs} type="${f.type}" value="${f.type==='password'?'':esc(value)}" ${f.type==='password'?'autocomplete="new-password"':''} ${f.name==='dateOfBirth'?`max="${new Date(Date.now()-86400000).toISOString().slice(0,10)}"`:''}>`;
  const help=locked?'Vínculo definido no cadastro.':f.name==='password'&&editing?'Deixe em branco para manter a senha atual.':f.type==='ref'?'Não encontrou a pessoa? Faça o cadastro na seção correspondente.':f.type==='datetime-local'?'Horário local do seu navegador.':f.help||'';
  return `<div class="field ${f.wide?'wide':''}"><label for="field-${f.name}">${f.label}${required?' <span class="required" aria-label="obrigatório">*</span>':''}</label>${input}<small id="help-${f.name}">${esc(help)}</small><span class="field-error" id="error-${f.name}"></span></div>`;
}
function openEditor(key,id){if(state.busy)return;const meta=resources[key],row=(state.data[key]||[]).find(r=>r.id===id);
  if(id&&!row){toast('Registro não encontrado. Atualize os dados.');return;}
  if(key==='consultations'&&row?.status!=='SCHEDULED'&&row){toast('Consultas encerradas não podem ser alteradas.');return;}
  editor.innerHTML=`<form id="record-form"><header class="dialog-head"><div><h2 id="dialog-title">${id?'Editar':key==='consultations'?'Agendar':'Cadastrar'} ${meta.singular}</h2><p>Os campos com * são obrigatórios.</p></div><button type="button" class="icon-btn" data-close aria-label="Fechar formulário">${icon('close')}</button></header><div class="dialog-body"><div class="error-box" id="form-error" role="alert"></div>${key==='consultations'?'<div class="info-strip">O encontro deve estar dentro de uma disponibilidade do profissional. Cancelar libera o horário; concluir exige que o encontro já tenha terminado.</div>':''}<div class="form-grid">${meta.fields.map(f=>formField(f,key,row)).join('')}</div></div><footer class="dialog-footer"><button type="button" class="btn" data-close>Cancelar</button><button type="submit" class="btn primary">${id?'Salvar alterações':key==='consultations'?'Agendar consulta':'Salvar cadastro'}</button></footer></form>`;
  $$('[data-close]',editor).forEach(b=>b.onclick=()=>editor.close());editor.showModal();
  $('#record-form').onsubmit=async event=>{
    event.preventDefault();const button=$('button[type=submit]',editor),errorBox=$('#form-error');errorBox.textContent='';$$('.field-error',editor).forEach(el=>el.textContent='');$$('[aria-invalid]',editor).forEach(el=>el.removeAttribute('aria-invalid'));const body={};
    try {
      for(const f of meta.fields){let value=$(`#field-${f.name}`,editor).value;if(f.name==='password'&&id&&!value)continue;if(f.type==='json'){try{value=JSON.parse(value);}catch{throw Object.assign(new Error('Confira a configuração: use um objeto JSON válido.'),{fields:{[f.name]:'JSON inválido.'}});}if(!value||Array.isArray(value)||typeof value!=='object')throw new Error('A configuração precisa ser um objeto JSON.');if(new TextEncoder().encode(JSON.stringify(value)).length>16384)throw new Error('A configuração deve ter até 16 KiB.');}else if(f.type==='datetime-local')value=new Date(value).toISOString();else if(f.type==='boolean')value=value==='true';else if(f.type!=='password')value=value.trim()||null;body[f.name]=value;}
      const start=body.scheduledStartAt||body.startAt,end=body.scheduledEndAt||body.endAt;
      if(start&&end&&new Date(end)<=new Date(start))throw new Error('O término deve ser posterior ao início.');
      if(body.password&&new TextEncoder().encode(body.password).length>72)throw new Error('A senha deve ter até 72 bytes UTF-8.');
      button.disabled=true;button.textContent='Salvando…';$$('[data-close]',editor).forEach(b=>b.disabled=true);
      await api(`${key}${id?`/${id}`:''}`,{method:id?'PUT':'POST',body});
      editor.close();toast(id?'Alterações salvas.':'Cadastro realizado.');if(state.route!==key)navigate(key);await refresh();
    }catch(error){if(!editor.open)return;errorBox.textContent=error.message;for(const [name,message]of Object.entries(error.fields||{})){const el=$(`#error-${name}`);if(el){el.textContent=message;$(`#field-${name}`).setAttribute('aria-invalid','true');}}$('.dialog-body',editor).scrollTop=0;button.disabled=false;button.textContent=id?'Salvar alterações':'Salvar cadastro';$$('[data-close]',editor).forEach(b=>b.disabled=false);}
  };
}
function openDetails(key,id){const row=(state.data[key]||[]).find(r=>r.id===id);if(!row)return;const meta=resources[key];editor.innerHTML=`<header class="dialog-head"><div><h2 id="dialog-title">Detalhes de ${meta.singular}</h2><p>Informações do cadastro</p></div><button class="icon-btn" data-close aria-label="Fechar detalhes">${icon('close')}</button></header><div class="dialog-body"><dl class="detail-list">${meta.fields.filter(f=>f.type!=='password').map(f=>{let value=row[f.name];if(f.type==='ref')value=recordName(f.ref,value);else if(f.type==='datetime-local')value=dateTime(value);else if(f.type==='boolean')value=key==='activities'?(value?'Ativa':'Inativa'):labels[value];else if(f.type==='select')value=(f.options||[]).find(o=>Array.isArray(o)&&o[0]===value)?.[1]||labels[value]||value;else if(f.type==='date')value=new Date(value+'T12:00:00').toLocaleDateString('pt-BR');return `<div class="${f.wide?'wide':''}"><dt>${f.label}</dt><dd>${f.type==='json'?`<pre>${esc(JSON.stringify(value,null,2))}</pre>`:esc(value||'Não informado')}</dd></div>`;}).join('')}<div class="wide"><dt>Identificador</dt><dd>${esc(row.id)}</dd></div><div><dt>Criado em</dt><dd>${esc(dateTime(row.createdAt))}</dd></div><div><dt>Atualizado em</dt><dd>${esc(dateTime(row.updatedAt))}</dd></div></dl></div><footer class="dialog-footer"><button class="btn" data-close>Fechar</button>${key==='consultations'&&row.status!=='SCHEDULED'?'':'<button class="btn primary" id="detail-edit">Editar cadastro</button>'}</footer>`;$$('[data-close]',editor).forEach(b=>b.onclick=()=>editor.close());if($('#detail-edit'))$('#detail-edit').onclick=()=>{editor.close();openEditor(key,id);};editor.showModal();}
function confirmDelete(key,id){const row=(state.data[key]||[]).find(r=>r.id===id);if(!row)return;const title=resources[key].singular;confirmation.innerHTML=`<header class="dialog-head"><h2 id="confirm-title">Excluir ${title}?</h2><button class="icon-btn" data-close aria-label="Fechar confirmação">${icon('close')}</button></header><div class="confirm-copy">Essa ação remove o registro e não pode ser desfeita. Se houver vínculos ou consultas que dependam dele, a exclusão será bloqueada.<div id="delete-error" class="error-box" role="alert"></div></div><footer class="dialog-footer"><button class="btn" data-close>Manter cadastro</button><button class="btn danger" id="confirm-delete">Excluir ${title}</button></footer>`;$$('[data-close]',confirmation).forEach(b=>b.onclick=()=>confirmation.close());confirmation.showModal();$('#confirm-delete').onclick=async()=>{const button=$('#confirm-delete');button.disabled=true;button.textContent='Excluindo…';$$('[data-close]',confirmation).forEach(b=>b.disabled=true);try{await api(`${key}/${id}`,{method:'DELETE'});confirmation.close();toast('Registro excluído.');await refresh();}catch(error){if(!confirmation.open)return;$('#delete-error').textContent=error.message;button.disabled=false;button.textContent=`Excluir ${title}`;$$('[data-close]',confirmation).forEach(b=>b.disabled=false);}};}
window.addEventListener('hashchange',()=>{
  if(!state.ready){renderLogin();return;}
  if(state.identity?.role!=='ADMIN')return;
  if(validRoute()!==state.route){state.route=validRoute();state.query='';state.filter='';state.date='';state.page=0;renderShell();renderMain();}
});
document.addEventListener('keydown',event=>{if(event.key==='Escape'&&$('.sidebar.open'))toggleMenu(false);});
renderLogin();
