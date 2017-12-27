# 葫芦娃期末作业

## 设想实现效果
1. 在开始之前，玩家可以在多种阵型中选择一种；
2. 选择阵型并按下`空格键`之后，我方按阵型出发；敌方以随机阵型出发；
3. 我方阵型的不同，一个是阵型的不同，还有一个是葫芦娃属性的不同——属性克制；
4. 游戏目标：
   - 保护爷爷；
   - 击败所有敌人


## 框架选择
采用与示例`huluwa`相似的GUI架构，即：一个继承了`JFrame`的类包含了一个继承了`JPanel`的类。

## 碰撞检测
`M*N`的二维数组的类型是`Things`，它有两种子类：
1. 空地设置为`Empty`；
2. 生命体设置为`Creature`。
  在`Ground`类中，有一个`M*N`的`things`数组。如果`things[i][j] instanceof Empty`,说明改地方是空地；否则为相应生命体。