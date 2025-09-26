# NUIXServer

## 部署

请保证python版本不小于3.11，可使用[conda](https://docs.anaconda.com/free/miniconda/index.html)创建虚拟环境。

安装pdm包管理工具：

```bash
pip install pdm
```

安装包：

```bash
pdm install
```

启动后台服务：

```bash
pdm run web
```

如果没有公网ip，请保证NUIXServer和NUIXClient位于同一局域网下。
