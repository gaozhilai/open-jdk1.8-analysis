# open-jdk1.8-analysis
JDK源码分析注释, 源码版本Open JDK1.8, 目的是让使用者节省阅读源码时间
# 使用方法
**IDEA配置方法**  
Idea -> File -> Project Structure -> SDKs -> Sourcepath -> 
点击+号后 -> 选择克隆的项目目录  
**Eclipse配置方法**  
Eclipse -> Windows -> Preferences -> Java -> Installed JREs -> Edit -> Source Attachment -> External location -> External Folder -> 选择克隆的项目目录

此时在任意的项目中点击进入JDK类文件时展示的就是此项目中添加了注释的源码  

ps 出于保留类文件原始行号结构不变考虑, 使用了大量的行尾注释, 实际使用中, 建议使用>=24寸显示器达到最佳阅读效果
# 阅读PR约定&开始参与本项目
1. 使用行尾注释或复用源文件空白行, 与平时开发习惯相左, 为了保持源文件行号不变.
**(2020/09/11 行号明确不能变化, 源文件与字节码指令行号对应信息存储在字节码文件LineNumberTable中。变化了会导致IDE无法准确断点调试, 推荐大家适当抹去不重要的英文注释或者跟在英文注释同行进行注释)**
2. 不能见名知意的参数, 逻辑复杂的语句给出简明注释, 其余能省则省, 方便读者抓住重点, 同时有自己思考的空间.
3. 如果是一个全新的没人注释过的类, 可以添加上注释的人的姓名, 日期, 时间, 防止他人重复工作.
4. 提交PR以完整的类作为最小单位, 经过review之后会合并到本仓库.
5. 代码中极个别不确定的或者有疑问的地方, 经长时间思考或者尝试理解依旧不能合理解释的地方, 可以在**注释起始位置标记@QUESTION**, 注解后方标明疑问说明, 方便后续统一处理, 建议**一个类文件中不超过两处.**