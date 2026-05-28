package com.mycompany.telecom.billing.util;

public class HtmlLayout {

    // ── Telnova SVG logo mark (falcon path from brand guide) ─────────────────
    public static final String LOGO_SVG =
        "<svg viewBox='0 0 100 100' style='width:28px;height:28px;'>" +
        "<defs><linearGradient id='tlg' x1='0%' y1='0%' x2='100%' y2='100%'>" +
        "<stop offset='0%' stop-color='#00d4ff'/><stop offset='100%' stop-color='#6366f1'/>" +
        "</linearGradient></defs>" +
        "<path d='M 15 60 L 35 45 L 50 50 L 65 30 L 80 25 L 70 45 L 85 50 L 55 60 L 50 75 L 35 60 Z' fill='url(#tlg)'/>" +
        "<path d='M 35 45 L 50 50 L 35 60 Z' fill='#0a0e27' opacity='0.3'/>" +
        "</svg>";

    // ── Logo wordmark HTML ─────────────────────────────────────────────────────
    public static final String LOGO_WORDMARK =
        "<span style='color:#fff;font-weight:700;'>Tel</span>" +
        "<span style='background:linear-gradient(135deg,#00d4ff,#6366f1);-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-weight:700;'>nova</span>";

    // ── Currency ───────────────────────────────────────────────────────────────
    public static final String EGP = "EGP";

    private static final String CSS = """
        :root{
          --bg:#0a0e27;--surface:#1a1f3a;--surface2:#1f2547;--border:#2d3561;
          --cyan:#00d4ff;--indigo:#6366f1;--green:#10b981;--orange:#f59e0b;
          --red:#ef4444;--text:#e8eaf6;--muted:#9ca3af;--radius:12px;
          --gradient:linear-gradient(135deg,#00d4ff,#6366f1);
          --shadow:0 4px 24px rgba(0,0,0,.4);
        }
        *{box-sizing:border-box;margin:0;padding:0}
        body{font-family:'Segoe UI',system-ui,sans-serif;background:var(--bg);
             color:var(--text);display:flex;min-height:100vh;}

        /* Sidebar */
        .sidebar{width:240px;background:var(--surface);border-right:1px solid var(--border);
                 display:flex;flex-direction:column;position:fixed;top:0;left:0;bottom:0;z-index:100;}
        .sidebar-logo{padding:22px 20px;display:flex;align-items:center;gap:10px;
                      border-bottom:1px solid var(--border);}
        .sidebar-logo .wordmark{font-size:18px;letter-spacing:-.3px;}
        .sidebar-nav{padding:16px 12px;flex:1;}
        .nav-label{font-size:10px;font-weight:700;letter-spacing:1.2px;color:var(--muted);
                   padding:8px 8px 6px;text-transform:uppercase;}
        .nav-item{display:flex;align-items:center;gap:10px;padding:10px 12px;
                  border-radius:8px;color:var(--muted);text-decoration:none;
                  font-size:13.5px;font-weight:500;transition:all .15s;margin-bottom:2px;}
        .nav-item:hover{background:var(--surface2);color:var(--text);}
        .nav-item.active{background:linear-gradient(135deg,rgba(0,212,255,.18),rgba(99,102,241,.18));
          color:var(--text);border:1px solid rgba(99,102,241,.35);}
        .nav-item .icon{font-size:16px;width:20px;text-align:center;}
        .sidebar-footer{padding:16px 12px;border-top:1px solid var(--border);}
        .logout-btn{display:flex;align-items:center;gap:10px;padding:10px 12px;
                    border-radius:8px;color:var(--red);text-decoration:none;
                    font-size:13.5px;font-weight:500;transition:background .15s;}
        .logout-btn:hover{background:rgba(239,68,68,.1);}

        /* Main */
        .main{margin-left:240px;flex:1;display:flex;flex-direction:column;min-height:100vh;}
        .topbar{height:58px;background:var(--surface);border-bottom:1px solid var(--border);
                display:flex;align-items:center;padding:0 28px;
                justify-content:space-between;position:sticky;top:0;z-index:50;}
        .page-title{font-size:16px;font-weight:600;}
        .admin-badge{display:flex;align-items:center;gap:10px;font-size:13px;color:var(--muted);}
        .admin-badge .avatar{width:32px;height:32px;border-radius:50%;
          background:var(--gradient);display:flex;align-items:center;
          justify-content:center;font-size:13px;font-weight:700;color:#fff;}
        .content{padding:28px;flex:1;}

        /* Cards */
        .card{background:var(--surface);border:1px solid var(--border);
              border-radius:var(--radius);box-shadow:var(--shadow);}
        .card-header{padding:18px 22px;border-bottom:1px solid var(--border);
                     display:flex;align-items:center;justify-content:space-between;}
        .card-title{font-size:15px;font-weight:600;}
        .card-body{padding:22px;}

        /* Stat cards */
        .stat-card{background:var(--surface);border:1px solid var(--border);
                   border-radius:var(--radius);padding:20px 22px;
                   display:flex;align-items:center;gap:16px;}
        .stat-icon{width:48px;height:48px;border-radius:12px;
                   display:flex;align-items:center;justify-content:center;font-size:22px;}
        .stat-icon.cyan  {background:rgba(0,212,255,.12);}
        .stat-icon.indigo{background:rgba(99,102,241,.12);}
        .stat-icon.green {background:rgba(16,185,129,.12);}
        .stat-icon.orange{background:rgba(245,158,11,.12);}
        .stat-label{font-size:12px;color:var(--muted);margin-bottom:4px;}
        .stat-value{font-size:24px;font-weight:700;}

        /* Table */
        .table-wrap{overflow-x:auto;}
        table{width:100%;border-collapse:collapse;font-size:13.5px;}
        thead tr{background:var(--surface2);border-bottom:1px solid var(--border);}
        th{padding:12px 16px;text-align:left;font-size:11px;font-weight:700;
           color:var(--indigo);letter-spacing:.8px;text-transform:uppercase;}
        td{padding:13px 16px;border-bottom:1px solid var(--border);}
        tbody tr:hover{background:rgba(99,102,241,.04);}
        tbody tr:last-child td{border-bottom:none;}

        /* Badges */
        .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 11px;
               border-radius:20px;font-size:11px;font-weight:600;}
        .badge-active   {background:rgba(16,185,129,.12); color:#10b981;border:1px solid rgba(16,185,129,.3);}
        .badge-suspended{background:rgba(245,158,11,.12); color:#f59e0b;border:1px solid rgba(245,158,11,.3);}
        .badge-deactive {background:rgba(239,68,68,.12);  color:#ef4444;border:1px solid rgba(239,68,68,.3);}
        .badge-onhold   {background:rgba(156,163,175,.12);color:#9ca3af;border:1px solid rgba(156,163,175,.3);}
        .badge-voice    {background:rgba(99,102,241,.12); color:#818cf8;border:1px solid rgba(99,102,241,.3);}
        .badge-data     {background:rgba(0,212,255,.12);  color:#00d4ff;border:1px solid rgba(0,212,255,.3);}
        .badge-sms      {background:rgba(245,158,11,.12); color:#f59e0b;border:1px solid rgba(245,158,11,.3);}

        /* Buttons */
        .btn{display:inline-flex;align-items:center;gap:6px;padding:9px 18px;
             border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;
             border:none;text-decoration:none;transition:all .15s;font-family:inherit;}
        .btn-primary{background:var(--gradient);color:#fff;box-shadow:0 2px 12px rgba(99,102,241,.3);}
        .btn-primary:hover{opacity:.88;box-shadow:0 4px 20px rgba(99,102,241,.5);}
        .btn-outline{background:transparent;color:var(--text);border:1px solid var(--border);}
        .btn-outline:hover{background:var(--surface2);}
        .btn-danger{background:#ef4444;color:#fff;}
        .btn-danger:hover{background:#dc2626;}
        .btn-sm{padding:5px 12px;font-size:12px;border-radius:6px;}

        /* Forms */
        .form-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:18px;}
        .form-group{display:flex;flex-direction:column;gap:6px;}
        .form-group.full{grid-column:1/-1;}
        label{font-size:12px;font-weight:600;color:var(--muted);letter-spacing:.5px;text-transform:uppercase;}
        input,select,textarea{background:var(--surface2);border:1px solid var(--border);
          border-radius:8px;color:var(--text);padding:10px 14px;font-size:13.5px;
          transition:border-color .15s;width:100%;font-family:inherit;}
        input:focus,select:focus,textarea:focus{outline:none;border-color:var(--indigo);
          box-shadow:0 0 0 3px rgba(99,102,241,.15);}
        select option{background:var(--surface2);}
        textarea{resize:vertical;min-height:80px;}
        .form-actions{display:flex;gap:10px;margin-top:8px;
                      padding-top:20px;border-top:1px solid var(--border);}

        /* Toast */
        .toast{position:fixed;bottom:24px;right:24px;background:var(--surface2);
               border:1px solid var(--border);border-radius:10px;padding:14px 20px;
               display:flex;align-items:center;gap:10px;font-size:13.5px;
               box-shadow:var(--shadow);animation:slideIn .3s ease;z-index:999;}
        .toast-success{border-color:rgba(16,185,129,.4);}
        @keyframes slideIn{from{transform:translateY(20px);opacity:0}to{transform:translateY(0);opacity:1}}

        /* Breadcrumb */
        .breadcrumb{display:flex;align-items:center;gap:6px;font-size:12.5px;
                    color:var(--muted);margin-bottom:20px;}
        .breadcrumb a{color:var(--muted);text-decoration:none;}
        .breadcrumb a:hover{color:var(--text);}
        .breadcrumb .sep{color:var(--border);}
        .breadcrumb .current{color:var(--text);}

        /* Empty */
        .empty{text-align:center;padding:60px 20px;color:var(--muted);}
        .empty-icon{font-size:48px;margin-bottom:12px;}
        .empty p{font-size:14px;}
    """;

    private static final String[][] NAV = {
        {"dashboard", "🏠", "Dashboard",        "/dashboard"},
        {"users",     "👤", "Customers",         "/users/"},
        {"contracts", "📋", "Contracts",         "/contracts/"},
        {"rateplans", "💳", "Rate Plans",         "/rateplans/"},
        {"packages",  "📦", "Service Packages",  "/packages/"},
    };

    public static String header(String pageTitle, String activeSection, String ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
          .append("<meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>").append(pageTitle).append(" — Telnova Admin</title>")
          .append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>")
          .append("<style>").append(CSS).append("</style></head><body>");

        // Sidebar
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

        // Main
        sb.append("<div class='main'>")
          .append("<div class='topbar'>")
          .append("<span class='page-title'>").append(pageTitle).append("</span>")
          .append("<div class='admin-badge'><div class='avatar'>A</div><span>Admin</span></div>")
          .append("</div><div class='content'>");

        return sb.toString();
    }

    public static String footer() {
        return """
            </div></div>
            <script>
              const t = document.querySelector('.toast');
              if (t) setTimeout(() => t.style.display='none', 3000);
              document.querySelectorAll('.delete-link').forEach(a => {
                a.addEventListener('click', e => {
                  if (!confirm('Delete this record? This cannot be undone.')) e.preventDefault();
                });
              });
            </script>
            </body></html>
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