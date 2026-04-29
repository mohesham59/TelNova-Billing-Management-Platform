/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.util;

/**
 *
 * @author Ali
 */
public class HtmlLayout {

    // ─── CSS ────────────────────────────────────────────────────────────────
    private static final String CSS = """
        :root {
            --bg:       #0f1117;
            --surface:  #1a1d27;
            --surface2: #22263a;
            --border:   #2e3248;
            --accent:   #6c63ff;
            --accent2:  #48cfad;
            --danger:   #ff5c6c;
            --warn:     #ffa94d;
            --text:     #e8eaf6;
            --muted:    #8b8fa8;
            --radius:   12px;
            --shadow:   0 4px 24px rgba(0,0,0,.35);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Inter', system-ui, sans-serif;
            background: var(--bg);
            color: var(--text);
            display: flex;
            min-height: 100vh;
        }

        /* ── Sidebar ── */
        .sidebar {
            width: 240px;
            background: var(--surface);
            border-right: 1px solid var(--border);
            display: flex;
            flex-direction: column;
            position: fixed;
            top: 0; left: 0; bottom: 0;
            z-index: 100;
        }
        .sidebar-logo {
            padding: 24px 20px 20px;
            display: flex; align-items: center; gap: 10px;
            border-bottom: 1px solid var(--border);
        }
        .sidebar-logo .logo-icon {
            width: 36px; height: 36px; border-radius: 10px;
            background: linear-gradient(135deg, var(--accent), var(--accent2));
            display: flex; align-items: center; justify-content: center;
            font-size: 18px;
        }
        .sidebar-logo span {
            font-size: 15px; font-weight: 700; letter-spacing: .3px;
        }
        .sidebar-logo small {
            display: block; font-size: 10px;
            color: var(--muted); font-weight: 400; margin-top: 1px;
        }
        .sidebar-nav { padding: 16px 12px; flex: 1; }
        .nav-label {
            font-size: 10px; font-weight: 600; letter-spacing: 1.2px;
            color: var(--muted); padding: 8px 8px 6px; text-transform: uppercase;
        }
        .nav-item {
            display: flex; align-items: center; gap: 10px;
            padding: 10px 12px; border-radius: 8px;
            color: var(--muted); text-decoration: none;
            font-size: 13.5px; font-weight: 500;
            transition: all .15s; margin-bottom: 2px;
        }
        .nav-item:hover { background: var(--surface2); color: var(--text); }
        .nav-item.active {
            background: linear-gradient(135deg,rgba(108,99,255,.25),rgba(72,207,173,.15));
            color: var(--text);
            border: 1px solid rgba(108,99,255,.3);
        }
        .nav-item .icon { font-size: 16px; width: 20px; text-align: center; }
        .sidebar-footer {
            padding: 16px 12px;
            border-top: 1px solid var(--border);
        }
        .logout-btn {
            display: flex; align-items: center; gap: 10px;
            padding: 10px 12px; border-radius: 8px;
            color: var(--danger); text-decoration: none;
            font-size: 13.5px; font-weight: 500;
            transition: background .15s;
        }
        .logout-btn:hover { background: rgba(255,92,108,.1); }

        /* ── Main ── */
        .main {
            margin-left: 240px;
            flex: 1;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        .topbar {
            height: 60px;
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            display: flex; align-items: center;
            padding: 0 28px;
            justify-content: space-between;
            position: sticky; top: 0; z-index: 50;
        }
        .page-title { font-size: 16px; font-weight: 600; }
        .admin-badge {
            display: flex; align-items: center; gap: 10px;
            font-size: 13px; color: var(--muted);
        }
        .admin-badge .avatar {
            width: 32px; height: 32px; border-radius: 50%;
            background: linear-gradient(135deg, var(--accent), var(--accent2));
            display: flex; align-items: center; justify-content: center;
            font-size: 13px; font-weight: 700; color: #fff;
        }
        .content { padding: 28px; flex: 1; }

        /* ── Cards ── */
        .card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
        }
        .card-header {
            padding: 18px 22px;
            border-bottom: 1px solid var(--border);
            display: flex; align-items: center; justify-content: space-between;
        }
        .card-title { font-size: 15px; font-weight: 600; }
        .card-body { padding: 22px; }

        /* ── Dashboard stat cards ── */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px,1fr));
            gap: 16px; margin-bottom: 24px;
        }
        .stat-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 20px 22px;
            display: flex; align-items: center; gap: 16px;
        }
        .stat-icon {
            width: 48px; height: 48px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            font-size: 22px;
        }
        .stat-icon.purple { background: rgba(108,99,255,.15); }
        .stat-icon.teal   { background: rgba(72,207,173,.15); }
        .stat-icon.orange { background: rgba(255,169,77,.15); }
        .stat-icon.red    { background: rgba(255,92,108,.15); }
        .stat-label { font-size: 12px; color: var(--muted); margin-bottom: 4px; }
        .stat-value { font-size: 24px; font-weight: 700; }

        /* ── Table ── */
        .table-wrap { overflow-x: auto; }
        table {
            width: 100%; border-collapse: collapse;
            font-size: 13.5px;
        }
        thead tr {
            background: var(--surface2);
            border-bottom: 1px solid var(--border);
        }
        th {
            padding: 12px 16px; text-align: left;
            font-size: 11px; font-weight: 600;
            color: var(--muted); letter-spacing: .8px;
            text-transform: uppercase;
        }
        td { padding: 13px 16px; border-bottom: 1px solid var(--border); }
        tbody tr:hover { background: rgba(255,255,255,.025); }
        tbody tr:last-child td { border-bottom: none; }

        /* ── Badges ── */
        .badge {
            display: inline-block; padding: 3px 10px; border-radius: 20px;
            font-size: 11px; font-weight: 600; letter-spacing: .3px;
        }
        .badge-active   { background: rgba(72,207,173,.15); color: var(--accent2); border: 1px solid rgba(72,207,173,.3); }
        .badge-suspended{ background: rgba(255,169,77,.15);  color: var(--warn);    border: 1px solid rgba(255,169,77,.3); }
        .badge-deactive { background: rgba(255,92,108,.15);  color: var(--danger);  border: 1px solid rgba(255,92,108,.3); }
        .badge-onhold   { background: rgba(139,143,168,.15); color: var(--muted);   border: 1px solid rgba(139,143,168,.3); }
        .badge-voice    { background: rgba(108,99,255,.15);  color: #a89dff;        border: 1px solid rgba(108,99,255,.3); }
        .badge-data     { background: rgba(72,207,173,.15);  color: var(--accent2); border: 1px solid rgba(72,207,173,.3); }
        .badge-sms      { background: rgba(255,169,77,.15);  color: var(--warn);    border: 1px solid rgba(255,169,77,.3); }

        /* ── Buttons ── */
        .btn {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 9px 18px; border-radius: 8px; font-size: 13px;
            font-weight: 600; cursor: pointer; border: none;
            text-decoration: none; transition: all .15s;
        }
        .btn-primary {
            background: var(--accent); color: #fff;
        }
        .btn-primary:hover { background: #7c74ff; }
        .btn-outline {
            background: transparent; color: var(--text);
            border: 1px solid var(--border);
        }
        .btn-outline:hover { background: var(--surface2); }
        .btn-danger { background: var(--danger); color: #fff; }
        .btn-danger:hover { background: #ff7080; }
        .btn-sm { padding: 5px 12px; font-size: 12px; border-radius: 6px; }
        .btn-icon { padding: 6px 8px; }

        /* ── Forms ── */
        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px,1fr));
            gap: 18px;
        }
        .form-group { display: flex; flex-direction: column; gap: 6px; }
        .form-group.full { grid-column: 1 / -1; }
        label { font-size: 12px; font-weight: 600; color: var(--muted); letter-spacing: .5px; }
        input, select, textarea {
            background: var(--surface2); border: 1px solid var(--border);
            border-radius: 8px; color: var(--text);
            padding: 10px 14px; font-size: 13.5px;
            transition: border-color .15s;
            width: 100%;
        }
        input:focus, select:focus, textarea:focus {
            outline: none; border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(108,99,255,.15);
        }
        select option { background: var(--surface2); }
        textarea { resize: vertical; min-height: 80px; }
        .form-actions {
            display: flex; gap: 10px; margin-top: 8px;
            padding-top: 20px; border-top: 1px solid var(--border);
        }

        /* ── Login ── */
        .login-page {
            min-height: 100vh; width: 100%;
            display: flex; align-items: center; justify-content: center;
            background: var(--bg);
        }
        .login-box {
            width: 100%; max-width: 400px;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 40px;
            box-shadow: var(--shadow);
        }
        .login-logo {
            text-align: center; margin-bottom: 28px;
        }
        .login-logo .big-icon {
            width: 60px; height: 60px; border-radius: 16px;
            background: linear-gradient(135deg, var(--accent), var(--accent2));
            display: flex; align-items: center; justify-content: center;
            font-size: 28px; margin: 0 auto 14px;
        }
        .login-logo h1 { font-size: 20px; font-weight: 700; }
        .login-logo p  { font-size: 13px; color: var(--muted); margin-top: 4px; }
        .login-box .form-group { margin-bottom: 16px; }
        .login-btn {
            width: 100%; padding: 12px;
            background: var(--accent); color: #fff;
            border: none; border-radius: 8px;
            font-size: 14px; font-weight: 600;
            cursor: pointer; margin-top: 6px;
            transition: background .15s;
        }
        .login-btn:hover { background: #7c74ff; }

        /* ── Alert / Toast ── */
        .alert {
            padding: 12px 16px; border-radius: 8px;
            font-size: 13.5px; margin-bottom: 20px;
            display: flex; align-items: center; gap: 10px;
        }
        .alert-error   { background: rgba(255,92,108,.12); border: 1px solid rgba(255,92,108,.3); color: #ff8a94; }
        .alert-success { background: rgba(72,207,173,.12); border: 1px solid rgba(72,207,173,.3); color: var(--accent2); }
        .toast {
            position: fixed; bottom: 24px; right: 24px;
            background: var(--surface2); border: 1px solid var(--border);
            border-radius: 10px; padding: 14px 20px;
            display: flex; align-items: center; gap: 10px;
            font-size: 13.5px; box-shadow: var(--shadow);
            animation: slideIn .3s ease; z-index: 999;
        }
        .toast-success { border-color: rgba(72,207,173,.4); }
        .toast-success .toast-icon { color: var(--accent2); }
        @keyframes slideIn {
            from { transform: translateY(20px); opacity: 0; }
            to   { transform: translateY(0);    opacity: 1; }
        }

        /* ── Empty state ── */
        .empty {
            text-align: center; padding: 60px 20px; color: var(--muted);
        }
        .empty .empty-icon { font-size: 48px; margin-bottom: 12px; }
        .empty p { font-size: 14px; }

        /* ── Breadcrumb ── */
        .breadcrumb {
            display: flex; align-items: center; gap: 6px;
            font-size: 12.5px; color: var(--muted); margin-bottom: 20px;
        }
        .breadcrumb a { color: var(--muted); text-decoration: none; }
        .breadcrumb a:hover { color: var(--text); }
        .breadcrumb .sep { color: var(--border); }
        .breadcrumb .current { color: var(--text); }
    """;

    // ─── NAV items ──────────────────────────────────────────────────────────
    private static final String[][] NAV = {
        {"dashboard", "🏠", "Dashboard", "/dashboard"},
        {"users", "👤", "Customers", "/users/"},
        {"contracts", "📋", "Contracts", "/contracts/"},
        {"rateplans", "💳", "Rate Plans", "/rateplans/"},
        {"packages", "📦", "Service Packages", "/packages/"},};

    // ─── Full page header ────────────────────────────────────────────────────
    public static String header(String pageTitle, String activeSection, String contextPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
                .append("<title>").append(pageTitle).append(" — Telecom Admin</title>")
                .append("<link rel='preconnect' href='https://fonts.googleapis.com'>")
                .append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>")
                .append("<style>").append(CSS).append("</style>")
                .append("</head><body>");

        // ── Sidebar
        sb.append("<aside class='sidebar'>")
                .append("<div class='sidebar-logo'>")
                .append("<div class='logo-icon'>📡</div>")
                .append("<div><span>TelecomBill</span><small>Admin Panel</small></div>")
                .append("</div>")
                .append("<nav class='sidebar-nav'>")
                .append("<div class='nav-label'>Main Menu</div>");

        for (String[] item : NAV) {
            String active = item[0].equals(activeSection) ? " active" : "";
            sb.append("<a href='").append(contextPath).append(item[3]).append("' class='nav-item").append(active).append("'>")
                    .append("<span class='icon'>").append(item[1]).append("</span>")
                    .append(item[2])
                    .append("</a>");
        }

        sb.append("</nav>")
                .append("<div class='sidebar-footer'>")
                .append("<a href='").append(contextPath).append("/logout' class='logout-btn'>")
                .append("<span class='icon'>🚪</span> Logout")
                .append("</a></div></aside>");

        // ── Main wrapper
        sb.append("<div class='main'>")
                .append("<div class='topbar'>")
                .append("<span class='page-title'>").append(pageTitle).append("</span>")
                .append("<div class='admin-badge'><div class='avatar'>A</div><span>Admin</span></div>")
                .append("</div>")
                .append("<div class='content'>");

        return sb.toString();
    }

    // ─── Footer ─────────────────────────────────────────────────────────────
    public static String footer() {
        return """
            </div></div>
            <script>
              // Auto-dismiss toast after 3 s
              const t = document.querySelector('.toast');
              if (t) setTimeout(() => t.style.display='none', 3000);
              // Confirm deletes
              document.querySelectorAll('.delete-link').forEach(a => {
                a.addEventListener('click', e => {
                  if (!confirm('Delete this record? This cannot be undone.')) e.preventDefault();
                });
              });
            </script>
            </body></html>
            """;
    }

    // ─── Toast helper ────────────────────────────────────────────────────────
    public static String toast(String param) {
        if (param == null) {
            return "";
        }
        String icon, cls, msg;
        switch (param) {
            case "saved" -> {
                icon = "✅";
                cls = "toast-success";
                msg = "Record saved successfully.";
            }
            case "deleted" -> {
                icon = "🗑️";
                cls = "toast-success";
                msg = "Record deleted.";
            }
            default -> {
                return "";
            }
        }
        return "<div class='toast " + cls + "'><span class='toast-icon'>" + icon + "</span>" + msg + "</div>";
    }

    // ─── Breadcrumb ─────────────────────────────────────────────────────────
    public static String breadcrumb(String... parts) {
        // parts: "Home","/dashboard","Users","/users/","New User",null
        StringBuilder sb = new StringBuilder("<div class='breadcrumb'>");
        for (int i = 0; i < parts.length; i += 2) {
            String label = parts[i];
            String url = (i + 1 < parts.length) ? parts[i + 1] : null;
            if (i > 0) {
                sb.append("<span class='sep'>›</span>");
            }
            if (url != null) {
                sb.append("<a href='").append(url).append("'>").append(label).append("</a>");
            } else {
                sb.append("<span class='current'>").append(label).append("</span>");
            }
        }
        return sb.append("</div>").toString();
    }

    // ─── Escape HTML ─────────────────────────────────────────────────────────
    public static String e(Object val) {
        if (val == null) {
            return "";
        }
        return val.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
