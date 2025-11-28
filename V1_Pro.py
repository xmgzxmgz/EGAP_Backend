# -*- coding: utf-8 -*-
"""
Created on Fri Nov 21 00:07:55 2025

@author: Huinan Xu

"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import shap
import base64
import io
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.tree import DecisionTreeClassifier, plot_tree, _tree
from sklearn.ensemble import RandomForestClassifier

# ==========================================
# 0. 环境设置
# ==========================================
plt.style.use('ggplot')
# 设置中文字体，防止乱码 (根据系统情况自动回退)
plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei', 'Arial Unicode MS', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

# ==========================================
# 1. 工具函数定义
# ==========================================
def fig_to_base64(fig):
    """将Matplotlib图像转换为HTML Base64字符串"""
    buf = io.BytesIO()
    fig.savefig(buf, format='png', bbox_inches='tight', dpi=120)
    buf.seek(0)
    img_str = base64.b64encode(buf.read()).decode('utf-8')
    plt.close(fig)
    return f'<img src="data:image/png;base64,{img_str}" style="max-width:100%;box-shadow:0 2px 5px rgba(0,0,0,0.1);">'

def tree_to_code(tree, feature_names, class_names):
    """将决策树逻辑转换为HTML格式的规则文本"""
    tree_ = tree.tree_
    feature_name = [
        feature_names[i] if i != _tree.TREE_UNDEFINED else "undefined!"
        for i in tree_.feature
    ]
    
    rules_list = []
    def recurse(node, depth):
        indent = "&nbsp;" * (depth * 6)
        if tree_.feature[node] != _tree.TREE_UNDEFINED:
            name = feature_name[node]
            threshold = tree_.threshold[node]
            # 保留逻辑高亮，去除装饰性图标
            rules_list.append(f"<div style='margin:2px 0;'>{indent}<span style='color:#2c3e50; font-weight:bold;'>IF</span> <strong>{name}</strong> <= {threshold:.3f}:</div>")
            recurse(tree_.children_left[node], depth + 1)
            rules_list.append(f"<div style='margin:2px 0;'>{indent}<span style='color:#2c3e50; font-weight:bold;'>ELSE</span> (<strong>{name}</strong> > {threshold:.3f}):</div>")
            recurse(tree_.children_right[node], depth + 1)
        else:
            class_idx = np.argmax(tree_.value[node])
            class_name = class_names[class_idx]
            # 移除 Emoji，保留清晰的缩进和高亮
            rules_list.append(f"<div style='background-color:#f0f2f5; padding:2px; display:inline-block; border-radius:4px;'>{indent}---> 归类为: <strong>{class_name}</strong></div>")

    recurse(0, 0)
    return "".join(rules_list)

# ==========================================
# 2. 数据模拟与聚类
# ==========================================
print("Step 1: 生成数据并执行全量聚类...")

# 模拟30个变量
np.random.seed(42)
n_samples = 1000
n_features = 30
# 随便起名：前25个是静态(Static)，后5个是动态(Dynamic)
col_names = [f'Static_{i}' for i in range(1, 26)] + [f'Dynamic_{i}' for i in range(1, 6)]
data = np.random.randn(n_samples, n_features)

# 埋入一些规律供聚类和筛选
data[:300, 0] += 4   # Static_1 显著
data[300:600, -1] += 5 # Dynamic_5 显著
data[600:, 10] -= 3  # Static_11 显著
data[:, 5] = np.random.rand(n_samples) # Static_6 纯噪音

df = pd.DataFrame(data, columns=col_names)

# 标准化并聚类
scaler = StandardScaler()
df_scaled = scaler.fit_transform(df)

k = 3
kmeans = KMeans(n_clusters=k, random_state=42, n_init=10)
df['Cluster'] = kmeans.fit_predict(df_scaled)
df['Cluster_Label'] = df['Cluster'].apply(lambda x: f'Cluster {x}')

# ==========================================
# 3. 自动筛选 Top 10 显著变量
# ==========================================
print("Step 2: 自动筛选 Top 10 显著变量...")

# 使用随机森林作为“特征筛选器”
selector_rf = RandomForestClassifier(n_estimators=100, random_state=42)
selector_rf.fit(df[col_names], df['Cluster'])

# 获取重要性并排序
importances = selector_rf.feature_importances_
indices = np.argsort(importances)[::-1] # 降序排列
top_10_indices = indices[:10]
top_10_features = [col_names[i] for i in top_10_indices]

print(f"筛选出的 Top 10 关键变量: {top_10_features}")

# ==========================================
# 4. 训练代理模型 (仅用 Top 10)
# ==========================================
print("Step 3: 基于 Top 10 变量训练代理模型...")

# 决策树 (用于生成可读规则)
dt_model = DecisionTreeClassifier(max_depth=3, random_state=42)
dt_model.fit(df[top_10_features], df['Cluster_Label'])

# 随机森林 (用于 SHAP 归因)
rf_explainer_model = RandomForestClassifier(n_estimators=100, max_depth=5, random_state=42)
rf_explainer_model.fit(df[top_10_features], df['Cluster'])

# ==========================================
# 5. 生成报告可视化组件
# ==========================================
print("Step 4: 生成可视化图表...")

# --- A. 聚类概览表 (Top 10 均值) ---
# 使用 background_gradient 高亮数值
profile_table = df.groupby('Cluster_Label')[top_10_features].mean().T
html_table = profile_table.style.background_gradient(cmap='RdYlBu_r', axis=1).format("{:.2f}").to_html()

# --- B. 变量分布箱线图 (Boxplot) ---
fig_box = plt.figure(figsize=(12, 8))
# 为了防止10个变量X轴拥挤，我们交换XY轴，做横向箱线图
df_melted = df.melt(id_vars='Cluster_Label', value_vars=top_10_features)
sns.boxplot(data=df_melted, y='variable', x='value', hue='Cluster_Label', palette='Set2', orient='h')
plt.title('Top 10 关键变量在各聚类中的分布对比')
plt.xlabel('标准化后数值')
plt.ylabel('变量名称')
plt.grid(axis='x', alpha=0.3)
html_boxplot = fig_to_base64(fig_box)

# --- C. 决策树规则图 ---
fig_tree = plt.figure(figsize=(16, 8))
plot_tree(dt_model, feature_names=top_10_features, class_names=dt_model.classes_, filled=True, rounded=True, fontsize=10)
plt.title('基于 Top 10 变量的聚类判别规则树')
html_tree_img = fig_to_base64(fig_tree)

# --- D. 决策树文字规则 ---
html_rules_text = tree_to_code(dt_model, top_10_features, dt_model.classes_)

# --- E. SHAP 分析 (核心) ---
print("Step 5: 计算 SHAP 值 (可能需要几秒钟)...")
explainer = shap.TreeExplainer(rf_explainer_model)
# 为了速度，如果数据量太大，可以只抽样计算，这里数据量小直接算全量
shap_values = explainer.shap_values(df[top_10_features])

shap_html_list = []
# 遍历每个 Cluster 生成解释图
for i in range(k):
    fig_shap = plt.figure(figsize=(10, 6))
    plt.title(f'Cluster {i} 的关键特征归因 (SHAP Summary)')
    
    # 处理 shap 版本兼容性 (List vs Array)
    sv = shap_values[i] if isinstance(shap_values, list) else shap_values[..., i]
    
    shap.summary_plot(sv, df[top_10_features], show=False, plot_type="dot")
    
    # 调整布局防止截断
    plt.tight_layout()
    # 移除图标，保留文字标题
    shap_html_list.append(f'<div class="shap-box"><h4>Cluster {i} 归因详情</h4>' + fig_to_base64(fig_shap) + '</div>')

# ==========================================
# 6. 组装 HTML 报告
# ==========================================
print("Step 6: 组装并保存 HTML 报告...")

# 修改CSS样式，使其更加商务简洁
html_content = f"""
<!DOCTYPE html>
<html>
<head>
    <title>聚类特征归因分析报告</title>
    <style>
        body {{ font-family: "Segoe UI", "Roboto", "Helvetica Neue", Arial, sans-serif; margin: 0; background-color: #ffffff; color: #333; }}
        .header {{ background-color: #2c3e50; color: white; padding: 30px 20px; text-align: left; border-bottom: 4px solid #34495e; }}
        .header h1 {{ margin: 0; font-size: 24px; font-weight: 600; }}
        .header p {{ margin-top: 5px; font-size: 14px; opacity: 0.9; }}
        .container {{ max-width: 1100px; margin: 30px auto; padding: 0 20px; }}
        .card {{ background: white; padding: 20px 0; border-bottom: 1px solid #eee; margin-bottom: 20px; }}
        h2 {{ color: #2c3e50; font-size: 18px; font-weight: 600; margin-top: 0; padding-bottom: 10px; border-bottom: 2px solid #eee; }}
        h4 {{ color: #555; margin-bottom: 10px; font-size: 16px; font-weight: 500; }}
        .rules-container {{ background: #f8f9fa; border: 1px solid #e9ecef; padding: 15px; border-radius: 4px; font-family: Consolas, Monaco, monospace; line-height: 1.6; font-size: 13px; overflow-x: auto; }}
        table {{ width: 100%; border-collapse: collapse; font-size: 13px; }}
        th, td {{ padding: 10px; text-align: center; border: 1px solid #ddd; }}
        .shap-box {{ margin-bottom: 40px; }}
        .footer {{ text-align: center; color: #999; font-size: 12px; padding: 20px 0; border-top: 1px solid #eee; margin-top: 40px; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>自动聚类与关键特征归因报告</h1>
        <p>分析维度：全量特征聚类 + Top 10 显著变量解释</p>
    </div>

    <div class="container">
        
        <div class="card">
            <h2>分析概况</h2>
            <p>本次分析共处理样本 <strong>{len(df)}</strong> 个，原始变量 <strong>{n_features}</strong> 个。</p>
            <p>模型自动识别出对分群影响最大的 <strong>10个变量</strong> 如下：</p>
            <div style="background:#f8f9fa; padding:10px; border:1px solid #e9ecef; border-radius:4px; color:#2c3e50;">
                {', '.join(top_10_features)}
            </div>
        </div>

        <div class="card">
            <h2>1. 聚类画像 (Top 10 特征均值)</h2>
            <p>下表展示了每个聚类在关键变量上的平均表现（颜色越深代表数值越高/低）：</p>
            <div style="overflow-x:auto;">
                {html_table}
            </div>
        </div>

        <div class="card">
            <h2>2. 特征分布对比</h2>
            <p>通过横向箱线图观察各聚类在关键变量上的数据分布差异：</p>
            {html_boxplot}
        </div>

        <div class="card">
            <h2>3. 聚类判定规则</h2>
            <p>通过决策树提炼出的硬性业务规则：</p>
            {html_tree_img}
            <div style="margin-top:20px;">
                <h4>规则文本逻辑：</h4>
                <div class="rules-container">
                    {html_rules_text}
                </div>
            </div>
        </div>

        <div class="card">
            <h2>4. SHAP 深度归因分析</h2>
            <p>针对每个 Cluster，分析推动样本被分入该组的核心变量。</p>
            <p style="font-size:12px; color:#666;">注：红色点=特征值高，蓝色点=特征值低。点在右侧(X>0)表示该特征促进分入该组。</p>
            {''.join(shap_html_list)}
        </div>

    </div>
    
    <div class="footer">
        Generated by Auto-Segmentation Analysis Pipeline
    </div>
</body>
</html>
"""

filename = "top10_risk_report_professional.html"
with open(filename, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"\n成功！包含Top 10变量解释的专业版报告已生成: {filename}")