<template>
  <div class="landing">
    <!-- Navbar -->
    <nav class="landing-nav">
      <div class="nav-inner">
        <div class="nav-logo">
          <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#DE2910"/>
            <path d="M8 10h16M8 16h10M8 22h12" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="23" cy="20" r="5" fill="white"/>
            <path d="M21 20l1.5 1.5L25 18" stroke="#DE2910" stroke-width="1.5" stroke-linecap="round"
                  stroke-linejoin="round"/>
          </svg>
          <span class="logo-name">GitHub Doc Translation</span>
        </div>
        <div class="nav-links">
          <a href="#features" class="nav-link" @click.prevent="scrollTo('features')">功能特性</a>
          <a href="#workflow" class="nav-link" @click.prevent="scrollTo('workflow')">工作流程</a>
          <a href="#languages" class="nav-link" @click.prevent="scrollTo('languages')">支持语言</a>
          <a href="/api/auth/github" class="btn-login">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path
                  d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            使用 GitHub 登录
          </a>
        </div>
      </div>
    </nav>

    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          为中文开源社区而生
        </div>
        <h1 class="hero-title">
          让你的开源项目<br/>
          <span class="hero-title-accent">被全世界看见</span>
        </h1>
        <p class="hero-subtitle">
          AI 驱动的 GitHub 文档多语言翻译平台<br/>
          一键接入，自动翻译，持续同步
        </p>
        <div class="hero-actions">
          <a href="/api/auth/github" class="btn-primary">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path
                  d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            使用 GitHub 登录，免费开始
          </a>
          <a href="#features" class="btn-outline" @click.prevent="scrollTo('features')">了解更多</a>
        </div>
        <p class="hero-tip">新用户免费获得 100,000 tokens 翻译额度</p>

        <!-- Demo Card -->
        <div class="demo-card">
          <div class="demo-header">
            <div class="demo-dots">
              <span></span><span></span><span></span>
            </div>
            <span class="demo-filename">README.md</span>
          </div>
          <div class="demo-body">
            <div class="demo-code-block">
              <div class="code-line source">
                <span class="line-badge zh">ZH</span>
                <span class="code-text">## 即时通讯服务 (IM-SERVICE)</span>
              </div>
              <div class="code-arrow">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#DE2910" stroke-width="2">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
                <span class="translate-tag">AI 翻译</span>
              </div>
              <div class="code-line translated">
                <span class="line-badge en">EN</span>
                <span class="code-text">## Instant Messaging Service (IM-SERVICE)</span>
              </div>
              <div class="code-line translated">
                <span class="line-badge ja">JA</span>
                <span class="code-text">## インスタントメッセージングサービス</span>
              </div>
              <div class="code-line translated">
                <span class="line-badge ko">KO</span>
                <span class="code-text">## 인스턴트 메시징 서비스</span>
              </div>
            </div>
            <div class="demo-pr-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#22C55E" stroke-width="2">
                <circle cx="18" cy="18" r="3"/>
                <circle cx="6" cy="6" r="3"/>
                <path d="M13 6h3a2 2 0 0 1 2 2v7"/>
                <line x1="6" y1="9" x2="6" y2="21"/>
              </svg>
              <span>自动创建 PR #42 已就绪</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features" id="features">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">为什么选择我们</h2>
          <p class="section-desc">为中文开源社区量身定制，帮助优质项目走向国际</p>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon" style="background:#fdefee">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#DE2910" stroke-width="2">
                <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
              </svg>
            </div>
            <h3>零配置接入</h3>
            <p>安装 GitHub App 即可使用，无需任何翻译或 i18n 知识，几分钟内完成接入</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon" style="background:#f0fdf4">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#22C55E" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <path d="M3 9h18M9 21V9"/>
              </svg>
            </div>
            <h3>可视化配置</h3>
            <p>树形目录选择翻译范围，所见即所得。支持 .gitdoc-ignore 精细控制</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon" style="background:#eff6ff">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path
                    d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
              </svg>
            </div>
            <h3>300+ AI 模型</h3>
            <p>通过 OpenRouter 接入 300+ 大模型，DeepSeek、Claude、Gemini 自由选择</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon" style="background:#fefce8">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2">
                <polyline points="23 4 23 10 17 10"/>
                <polyline points="1 20 1 14 7 14"/>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
              </svg>
            </div>
            <h3>自动增量同步</h3>
            <p>Webhook 实时监听文档变更，自动增量翻译，零维护成本</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon" style="background:#f5f3ff">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#8B5CF6" stroke-width="2">
                <circle cx="18" cy="18" r="3"/>
                <circle cx="6" cy="6" r="3"/>
                <path d="M13 6h3a2 2 0 0 1 2 2v7"/>
                <line x1="6" y1="9" x2="6" y2="21"/>
              </svg>
            </div>
            <h3>PR 工作流</h3>
            <p>翻译结果以 PR 形式提交，维护者通过标准 Review 流程审核合并</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon" style="background:#fff7ed">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <h3>Markdown 感知</h3>
            <p>翻译时完整保留 Markdown 格式、代码块、链接，不破坏文档结构</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Workflow -->
    <section class="workflow" id="workflow">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">四步开始翻译</h2>
          <p class="section-desc">从接入到 PR 就绪，最快 5 分钟</p>
        </div>
        <div class="workflow-steps">
          <div class="workflow-step">
            <div class="step-number">01</div>
            <div class="step-content">
              <h3>连接仓库</h3>
              <p>GitHub 登录并安装 GitHub App，授权需要翻译的仓库</p>
            </div>
          </div>
          <div class="workflow-arrow">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </div>
          <div class="workflow-step">
            <div class="step-number">02</div>
            <div class="step-content">
              <h3>选择文件</h3>
              <p>可视化树形目录选择需要翻译的 Markdown 文件</p>
            </div>
          </div>
          <div class="workflow-arrow">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </div>
          <div class="workflow-step">
            <div class="step-number">03</div>
            <div class="step-content">
              <h3>AI 翻译</h3>
              <p>选择 AI 模型和目标语言，平台自动并发翻译所有文件</p>
            </div>
          </div>
          <div class="workflow-arrow">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </div>
          <div class="workflow-step">
            <div class="step-number">04</div>
            <div class="step-content">
              <h3>自动 PR</h3>
              <p>翻译完成后自动提交 PR 到原仓库，合并即可</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Languages -->
    <section class="languages-section" id="languages">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">支持 10+ 种主流语言</h2>
          <p class="section-desc">覆盖全球主要开发者社区</p>
        </div>
        <div class="lang-grid">
          <div v-for="lang in supportedLangs" :key="lang.code" class="lang-card">
            <span class="lang-flag">{{ lang.flag }}</span>
            <span class="lang-name">{{ lang.name }}</span>
            <span class="lang-code">{{ lang.code }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <div class="cta-inner">
        <h2 class="cta-title">现在就开始，让你的开源项目走向世界</h2>
        <p class="cta-desc">免费获得 100,000 tokens 翻译额度，无需信用卡</p>
        <a href="/api/auth/github" class="btn-primary btn-large">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path
                d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          免费开始
        </a>
      </div>
    </section>

    <!-- Footer -->
    <footer class="landing-footer">
      <div class="footer-inner">
        <span>© 2026 GitHub Doc Translation</span>
        <div class="footer-links">
          <a href="https://github.com" target="_blank">GitHub</a>
          <a href="#">文档</a>
          <a href="#">反馈</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (!el) return
  const navHeight = 62
  const top = el.getBoundingClientRect().top + window.scrollY - navHeight
  window.scrollTo({ top, behavior: 'smooth' })
}

const supportedLangs = [
  { code: 'en', name: 'English', flag: '🇺🇸' },
  { code: 'ja', name: '日本語', flag: '🇯🇵' },
  { code: 'ko', name: '한국어', flag: '🇰🇷' },
  { code: 'es', name: 'Español', flag: '🇪🇸' },
  { code: 'fr', name: 'Français', flag: '🇫🇷' },
  { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
  { code: 'ru', name: 'Русский', flag: '🇷🇺' },
  { code: 'pt', name: 'Português', flag: '🇧🇷' },
  { code: 'ar', name: 'العربية', flag: '🇸🇦' },
  { code: 'hi', name: 'हिन्दी', flag: '🇮🇳' },
]
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: #FAFBFC;
  font-family: var(--font-primary);
}

/* ── Navbar ── */
.landing-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(250, 251, 252, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 28px;
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-link {
  font-size: 13.5px;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 6px 12px;
  border-radius: var(--border-radius-md);
  transition: background 0.15s, color 0.15s;
}

.nav-link:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.btn-login {
  display: flex;
  align-items: center;
  gap: 7px;
  background: #18181B;
  color: #FAFAFA;
  padding: 8px 16px;
  border-radius: var(--border-radius-md);
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: -0.01em;
  transition: background 0.15s, box-shadow 0.15s;
  box-shadow: var(--shadow-sm);
  margin-left: 6px;
}

.btn-login:hover {
  background: #27272A;
  box-shadow: var(--shadow-md);
}

/* ── Hero ── */
.hero {
  padding: 88px 24px 64px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(0, 0, 0, 0.04) 1px, transparent 1px);
  background-size: 22px 22px;
  pointer-events: none;
}

.hero::after {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(ellipse at center top, rgba(222, 41, 16, 0.06) 0%, transparent 70%);
  pointer-events: none;
}

.hero-inner {
  max-width: 820px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: var(--chinese-red-light);
  border: 1px solid var(--chinese-red-border);
  color: var(--chinese-red);
  padding: 5px 14px;
  border-radius: var(--border-radius-full);
  font-size: 12.5px;
  font-weight: 600;
  margin-bottom: 28px;
  letter-spacing: 0.01em;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--chinese-red);
  animation: pulse-dot 2s ease-in-out infinite;
}

.hero-title {
  font-size: 54px;
  font-weight: 800;
  line-height: 1.15;
  color: var(--text-primary);
  margin: 0 0 22px;
  letter-spacing: -0.03em;
}

.hero-title-accent {
  color: var(--chinese-red);
  background: linear-gradient(135deg, #DE2910 0%, #FF5733 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  line-height: 1.75;
  margin: 0 0 36px;
  font-weight: 400;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 14px;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--chinese-red);
  color: white;
  padding: 12px 26px;
  border-radius: var(--border-radius-md);
  text-decoration: none;
  font-size: 14.5px;
  font-weight: 600;
  letter-spacing: -0.01em;
  transition: background 0.2s, transform 0.15s, box-shadow 0.2s;
  box-shadow: var(--shadow-red);
}

.btn-primary:hover {
  background: var(--chinese-red-hover);
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(222, 41, 16, 0.32);
}

.btn-primary.btn-large {
  padding: 14px 32px;
  font-size: 15.5px;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-card);
  color: var(--text-secondary);
  padding: 12px 24px;
  border-radius: var(--border-radius-md);
  text-decoration: none;
  font-size: 14.5px;
  font-weight: 500;
  border: 1px solid var(--border-color);
  transition: border-color 0.15s, background 0.15s, color 0.15s, box-shadow 0.15s;
  box-shadow: var(--shadow-xs);
}

.btn-outline:hover {
  border-color: var(--border-color-hover);
  background: var(--bg-hover);
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
}

.hero-tip {
  font-size: 12.5px;
  color: var(--text-muted);
  margin: 0 0 44px;
}

/* ── Demo Card ── */
.demo-card {
  background: #161B22;
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  text-align: left;
  box-shadow: var(--shadow-xl), 0 0 0 1px rgba(255,255,255,0.05);
  border: 1px solid #30363D;
}

.demo-header {
  background: #21262D;
  padding: 11px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #30363D;
}

.demo-dots {
  display: flex;
  gap: 6px;
}

.demo-dots span {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}

.demo-dots span:first-child { background: #FF5F56; }
.demo-dots span:nth-child(2) { background: #FFBD2E; }
.demo-dots span:nth-child(3) { background: #27C93F; }

.demo-filename {
  font-size: 12px;
  color: #7D8590;
  font-family: var(--font-mono);
  letter-spacing: 0.01em;
}

.demo-body {
  padding: 20px 22px;
}

.demo-code-block {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.code-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.code-line.source .line-badge {
  background: #30363D;
  color: #7D8590;
}

.code-line.translated .line-badge {
  color: white;
}

.line-badge {
  font-size: 10px;
  font-weight: 700;
  font-family: var(--font-mono);
  padding: 2px 6px;
  border-radius: var(--border-radius-xs);
  min-width: 28px;
  text-align: center;
  letter-spacing: 0.02em;
}

.line-badge.en { background: #1B4FCC; }
.line-badge.ja { background: #9B1A1A; }
.line-badge.ko { background: #065F46; }

.code-text {
  font-family: var(--font-mono);
  font-size: 13px;
  color: #E6EDF3;
}

.code-line.source .code-text {
  color: #7D8590;
}

.code-arrow {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.translate-tag {
  font-size: 11px;
  color: #FF6B4A;
  font-family: var(--font-mono);
  background: rgba(222, 41, 16, 0.18);
  padding: 2px 8px;
  border-radius: var(--border-radius-xs);
  border: 1px solid rgba(222, 41, 16, 0.25);
}

.demo-pr-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 18px;
  padding: 9px 14px;
  background: rgba(22, 163, 74, 0.1);
  border: 1px solid rgba(22, 163, 74, 0.25);
  border-radius: var(--border-radius-md);
  font-size: 12px;
  color: #4ADE80;
  font-family: var(--font-mono);
}

/* ── Section Commons ── */
.section-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 88px 28px;
}

.section-header {
  text-align: center;
  margin-bottom: 56px;
}

.section-label {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--chinese-red);
  margin-bottom: 12px;
}

.section-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 14px;
  letter-spacing: -0.025em;
  line-height: 1.2;
}

.section-desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.65;
}

/* ── Features ── */
.features {
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 26px;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  background: #FAFBFD;
  transition: box-shadow 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.feature-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-color-hover);
  transform: translateY(-2px);
  background: var(--bg-card);
}

.feature-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  border: 1px solid transparent;
}

.feature-card h3 {
  font-size: 15.5px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.feature-card p {
  font-size: 13.5px;
  color: var(--text-secondary);
  line-height: 1.65;
  margin: 0;
}

/* ── Workflow ── */
.workflow-steps {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 8px;
}

.workflow-step {
  flex: 1;
  max-width: 210px;
  text-align: center;
  padding: 0 8px;
}

.step-number {
  font-size: 44px;
  font-weight: 800;
  font-family: var(--font-mono);
  color: var(--chinese-red);
  opacity: 0.12;
  line-height: 1;
  margin-bottom: 10px;
  letter-spacing: -0.03em;
}

.step-content h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
  letter-spacing: -0.01em;
}

.step-content p {
  font-size: 13.5px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.workflow-arrow {
  flex-shrink: 0;
  padding-top: 14px;
  opacity: 0.4;
}

/* ── Languages ── */
.languages-section {
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.lang-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.lang-card {
  display: flex;
  align-items: center;
  gap: 9px;
  background: #FAFBFD;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
  padding: 10px 18px;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
  cursor: default;
}

.lang-card:hover {
  border-color: var(--chinese-red-border);
  background: var(--chinese-red-light);
  box-shadow: var(--shadow-xs);
}

.lang-flag {
  font-size: 19px;
  line-height: 1;
}

.lang-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.lang-code {
  font-size: 11px;
  font-family: var(--font-mono);
  color: var(--text-muted);
  background: var(--bg-subtle);
  padding: 2px 7px;
  border-radius: var(--border-radius-xs);
  letter-spacing: 0.02em;
}

/* ── CTA Section ── */
.cta-section {
  padding: 96px 24px;
  text-align: center;
  background: #FAFBFC;
  position: relative;
  overflow: hidden;
}

.cta-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(0, 0, 0, 0.035) 1px, transparent 1px);
  background-size: 22px 22px;
  pointer-events: none;
}

.cta-inner {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 34px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 14px;
  line-height: 1.25;
  letter-spacing: -0.025em;
}

.cta-desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0 0 36px;
  line-height: 1.65;
}

/* ── Footer ── */
.landing-footer {
  background: #18181B;
  padding: 28px 24px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #71717A;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-links a {
  color: #71717A;
  text-decoration: none;
  transition: color 0.15s;
}

.footer-links a:hover {
  color: #A1A1AA;
}
</style>
