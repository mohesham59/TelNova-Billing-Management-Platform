package com.mycompany.telecom.billing.util;

public class HtmlLayout {

    public static final String LOGO_SVG =
        "<svg viewBox='0 0 100 100' style='width:28px;height:28px;'>" +
        "<defs><linearGradient id='tlg' x1='0%' y1='0%' x2='100%' y2='100%'>" +
        "<stop offset='0%' stop-color='#60b8f5'/><stop offset='100%' stop-color='#1a56db'/>" +
        "</linearGradient></defs>" +
        "<path d='M 15 60 L 35 45 L 50 50 L 65 30 L 80 25 L 70 45 L 85 50 L 55 60 L 50 75 L 35 60 Z' fill='url(#tlg)'/>" +
        "<path d='M 35 45 L 50 50 L 35 60 Z' fill='#060d1f' opacity='0.3'/>" +
        "</svg>";

    public static final String LOGO_WORDMARK =
        "<span style='color:#e8f4ff;font-weight:700;'>Tel</span>" +
        "<span style='background:linear-gradient(135deg,#60b8f5,#1a56db);-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-weight:700;'>nova</span>";

    public static final String EGP = "EGP";

    private static final String CSS = """
        @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:wght@300;400;500&display=swap');
        :root{
          --bg:#060d1f;--surface:#0b1630;--surface2:#0f1e3d;
          --border:rgba(255,255,255,.07);--border2:rgba(26,86,219,.4);
          --blue-light:#bfdbfe;--blue-mid:#60b8f5;--blue:#2d8fe0;
          --blue-deep:#1a56db;--blue-dark:#1e3a8a;
          --green:#10b981;--orange:#f59e0b;--red:#ef4444;
          --text:#e8f4ff;--muted:#7b9ec8;--radius:14px;
          --gradient:linear-gradient(135deg,#2d8fe0,#1a56db);
          --shadow:0 8px 32px rgba(0,0,0,.5);
        }
        *{box-sizing:border-box;margin:0;padding:0}
        body{
          font-family:'DM Sans',system-ui,sans-serif;
          background:var(--bg);color:var(--text);
          display:flex;min-height:100vh;
          background-image:
            radial-gradient(ellipse 55% 45% at 5% 0%,rgba(26,86,219,.12) 0%,transparent 60%),
            radial-gradient(ellipse 45% 40% at 95% 100%,rgba(45,143,224,.08) 0%,transparent 55%);
        }
        .sidebar{width:240px;background:rgba(11,22,48,.97);border-right:1px solid var(--border);
          display:flex;flex-direction:column;position:fixed;top:0;left:0;bottom:0;z-index:100;backdrop-filter:blur(20px);}
        .sidebar-logo{padding:22px 20px;display:flex;align-items:center;gap:10px;border-bottom:1px solid var(--border);}
        .sidebar-logo .wordmark{font-size:18px;letter-spacing:-.3px;}
        .sidebar-nav{padding:16px 12px;flex:1;}
        .nav-label{font-size:10px;font-weight:700;letter-spacing:1.5px;color:var(--muted);
          padding:8px 8px 6px;text-transform:uppercase;font-family:'Syne',sans-serif;}
        .nav-item{display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:9px;
          color:var(--muted);text-decoration:none;font-size:13.5px;font-weight:500;transition:all .18s;margin-bottom:2px;}
        .nav-item:hover{background:rgba(26,86,219,.12);color:var(--text);}
        .nav-item.active{background:linear-gradient(135deg,rgba(26,86,219,.22),rgba(45,143,224,.15));
          color:var(--blue-mid);border:1px solid rgba(26,86,219,.35);box-shadow:0 0 16px rgba(26,86,219,.1);}
        .nav-item .icon{font-size:16px;width:20px;text-align:center;}
        .sidebar-footer{padding:16px 12px;border-top:1px solid var(--border);}
        .logout-btn{display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:9px;
          color:var(--red);text-decoration:none;font-size:13.5px;font-weight:500;transition:background .15s;}
        .logout-btn:hover{background:rgba(239,68,68,.1);}
        .main{margin-left:240px;flex:1;display:flex;flex-direction:column;min-height:100vh;}
        .topbar{height:60px;background:rgba(11,22,48,.92);border-bottom:1px solid var(--border);
          display:flex;align-items:center;padding:0 28px;justify-content:space-between;
          position:sticky;top:0;z-index:50;backdrop-filter:blur(20px);}
        .page-title{font-family:'Syne',sans-serif;font-size:16px;font-weight:700;color:var(--text);}
        .admin-badge{display:flex;align-items:center;gap:10px;font-size:13px;color:var(--muted);}
        .admin-badge .avatar{width:34px;height:34px;border-radius:10px;background:var(--gradient);
          display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;
          color:#fff;box-shadow:0 0 16px rgba(26,86,219,.4);}
        .content{padding:28px;flex:1;}
        .card{background:rgba(255,255,255,.03);border:1px solid var(--border);
          border-radius:var(--radius);box-shadow:var(--shadow);backdrop-filter:blur(10px);}
        .card-header{padding:18px 22px;border-bottom:1px solid var(--border);
          display:flex;align-items:center;justify-content:space-between;}
        .card-title{font-family:'Syne',sans-serif;font-size:15px;font-weight:700;}
        .card-body{padding:22px;}
        .stat-card{background:rgba(255,255,255,.03);border:1px solid var(--border);
          border-radius:var(--radius);padding:20px 22px;display:flex;align-items:center;gap:16px;
          transition:border-color .2s,transform .2s;}
        .stat-card:hover{border-color:var(--border2);transform:translateY(-2px);}
        .stat-icon{width:48px;height:48px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:22px;}
        .stat-icon.cyan  {background:rgba(45,143,224,.15);border:1px solid rgba(45,143,224,.25);}
        .stat-icon.indigo{background:rgba(26,86,219,.15);border:1px solid rgba(26,86,219,.25);}
        .stat-icon.green {background:rgba(16,185,129,.12);border:1px solid rgba(16,185,129,.2);}
        .stat-icon.orange{background:rgba(245,158,11,.12);border:1px solid rgba(245,158,11,.2);}
        .stat-label{font-size:12px;color:var(--muted);margin-bottom:4px;}
        .stat-value{font-family:'Syne',sans-serif;font-size:24px;font-weight:700;}
        .table-wrap{overflow-x:auto;}
        table{width:100%;border-collapse:collapse;font-size:13.5px;}
        thead tr{background:rgba(26,86,219,.07);border-bottom:1px solid var(--border);}
        th{padding:12px 16px;text-align:left;font-size:10.5px;font-weight:700;color:var(--blue-mid);
          letter-spacing:1px;text-transform:uppercase;font-family:'Syne',sans-serif;}
        td{padding:13px 16px;border-bottom:1px solid var(--border);}
        tbody tr:hover{background:rgba(26,86,219,.05);}
        tbody tr:last-child td{border-bottom:none;}
        .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 11px;border-radius:20px;font-size:11px;font-weight:600;}
        .badge-active   {background:rgba(16,185,129,.12);color:#10b981;border:1px solid rgba(16,185,129,.3);}
        .badge-suspended{background:rgba(245,158,11,.12);color:#f59e0b;border:1px solid rgba(245,158,11,.3);}
        .badge-deactive {background:rgba(239,68,68,.12);color:#ef4444;border:1px solid rgba(239,68,68,.3);}
        .badge-onhold   {background:rgba(156,163,175,.12);color:#9ca3af;border:1px solid rgba(156,163,175,.3);}
        .badge-voice    {background:rgba(45,143,224,.12);color:#60b8f5;border:1px solid rgba(45,143,224,.3);}
        .badge-data     {background:rgba(26,86,219,.12);color:#93c5fd;border:1px solid rgba(26,86,219,.3);}
        .badge-sms      {background:rgba(96,184,245,.12);color:#bfdbfe;border:1px solid rgba(96,184,245,.3);}
        .btn{display:inline-flex;align-items:center;gap:6px;padding:9px 18px;border-radius:9px;
          font-size:13px;font-weight:600;cursor:pointer;border:none;text-decoration:none;
          transition:all .18s;font-family:inherit;}
        .btn-primary{background:var(--gradient);color:#fff;box-shadow:0 0 20px rgba(26,86,219,.3);}
        .btn-primary:hover{box-shadow:0 0 32px rgba(26,86,219,.55);transform:translateY(-1px);}
        .btn-outline{background:rgba(255,255,255,.04);color:var(--text);border:1px solid var(--border);}
        .btn-outline:hover{border-color:var(--border2);background:rgba(26,86,219,.08);}
        .btn-danger{background:rgba(239,68,68,.12);color:#f87171;border:1px solid rgba(239,68,68,.3);}
        .btn-danger:hover{background:rgba(239,68,68,.22);}
        .btn-sm{padding:5px 12px;font-size:12px;border-radius:7px;}
        .form-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:18px;}
        .form-group{display:flex;flex-direction:column;gap:6px;}
        .form-group.full{grid-column:1/-1;}
        label{font-size:11px;font-weight:700;color:var(--muted);letter-spacing:.8px;
          text-transform:uppercase;font-family:'Syne',sans-serif;}
        input,select,textarea{background:rgba(255,255,255,.04);border:1px solid var(--border);
          border-radius:9px;color:var(--text);padding:10px 14px;font-size:13.5px;
          transition:border-color .15s;width:100%;font-family:inherit;}
        input:focus,select:focus,textarea:focus{outline:none;border-color:var(--blue);
          box-shadow:0 0 0 3px rgba(26,86,219,.15);}
        select option{background:#0b1630;}
        textarea{resize:vertical;min-height:80px;}
        .form-actions{display:flex;gap:10px;margin-top:8px;padding-top:20px;border-top:1px solid var(--border);}
        .toast{position:fixed;bottom:24px;right:24px;background:rgba(11,22,48,.97);
          border:1px solid var(--border);border-radius:10px;padding:14px 20px;
          display:flex;align-items:center;gap:10px;font-size:13.5px;
          box-shadow:var(--shadow);animation:slideIn .3s ease;z-index:999;backdrop-filter:blur(20px);}
        .toast-success{border-color:rgba(16,185,129,.4);}
        @keyframes slideIn{from{transform:translateY(20px);opacity:0}to{transform:translateY(0);opacity:1}}
        .breadcrumb{display:flex;align-items:center;gap:6px;font-size:12.5px;color:var(--muted);margin-bottom:20px;}
        .breadcrumb a{color:var(--muted);text-decoration:none;}
        .breadcrumb a:hover{color:var(--blue-mid);}
        .breadcrumb .sep{color:var(--border);}
        .breadcrumb .current{color:var(--text);}
        .empty{text-align:center;padding:60px 20px;color:var(--muted);}
        .empty-icon{font-size:48px;margin-bottom:12px;}
        .empty p{font-size:14px;}
        ::-webkit-scrollbar{width:6px;height:6px;}
        ::-webkit-scrollbar-track{background:var(--bg);}
        ::-webkit-scrollbar-thumb{background:rgba(26,86,219,.3);border-radius:99px;}
        ::-webkit-scrollbar-thumb:hover{background:rgba(26,86,219,.5);}
    """;

    private static final String[][] NAV = {
        {"dashboard","🏠","Dashboard",       "/dashboard"},
        {"users",    "👤","Customers",        "/users/"},
        {"contracts","📋","Contracts",        "/contracts/"},
        {"rateplans","💳","Rate Plans",        "/rateplans/"},
        {"packages", "📦","Service Packages", "/packages/"},
    };

    public static String header(String pageTitle, String activeSection, String ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
          .append("<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>").append(pageTitle).append(" — Telnova Admin</title>")
          .append("<style>").append(CSS).append("</style></head><body>");
        sb.append("<aside class='sidebar'>")
          .append("<div class='sidebar-logo'>").append(LOGO_SVG)
          .append("<span class='wordmark'>").append(LOGO_WORDMARK).append("</span></div>")
          .append("<nav class='sidebar-nav'><div class='nav-label'>Main Menu</div>");
        for (String[] item : NAV) {
            String active = item[0].equals(activeSection) ? " active" : "";
            sb.append("<a href='").append(ctx).append(item[3])
              .append("' class='nav-item").append(active).append("'>")
              .append("<span class='icon'>").append(item[1]).append("</span>")
              .append(item[2]).append("</a>");
        }
        sb.append("</nav><div class='sidebar-footer'>")
          .append("<a href='").append(ctx).append("/logout' class='logout-btn'>")
          .append("<span class='icon'>🚪</span> Logout</a></div></aside>");
        sb.append("<div class='main'><div class='topbar'>")
          .append("<span class='page-title'>").append(pageTitle).append("</span>")
          .append("<div class='admin-badge'><div class='avatar'>A</div><span>Admin</span></div>")
          .append("</div><div class='content'>");
        return sb.toString();
    }

    public static String footer() {
        return """
            </div></div>
            <script>
              const t=document.querySelector('.toast');
              if(t) setTimeout(()=>t.style.display='none',3000);
              document.querySelectorAll('.delete-link').forEach(a=>{
                a.addEventListener('click',e=>{
                  if(!confirm('Delete this record? This cannot be undone.')) e.preventDefault();
                });
              });
            </script></body></html>
            """;
    }

    public static String toast(String param) {
        if (param == null) return "";
        return switch (param) {
            case "saved"   -> "<div class='toast toast-success'>✅ Record saved successfully.</div>";
            case "deleted" -> "<div class='toast toast-success'>🗑️ Record deleted.</div>";
            default        -> "";
        };
    }

    public static String breadcrumb(String... parts) {
        StringBuilder sb = new StringBuilder("<div class='breadcrumb'>");
        for (int i = 0; i < parts.length; i += 2) {
            String label = parts[i];
            String url   = (i + 1 < parts.length) ? parts[i + 1] : null;
            if (i > 0) sb.append("<span class='sep'>›</span>");
            if (url != null)
                sb.append("<a href='").append(url).append("'>").append(label).append("</a>");
            else
                sb.append("<span class='current'>").append(label).append("</span>");
        }
        return sb.append("</div>").toString();
    }

    public static String e(Object val) {
        if (val == null) return "";
        return val.toString()
            .replace("&","&amp;").replace("<","&lt;")
            .replace(">","&gt;").replace("\"","&quot;");
    }
}