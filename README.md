# 合理排序

If you do not understand Chinese, you can read [English version](README-en.md).

你是否发现，在创造模式下，物品栏内的物品排序方式特别乱，找到你需要的东西特别不容易？

本模组通过调整物品注册表迭代的顺序，来调整物品的排序方式，使得物品排序方式更为合理。由于是在物品注册表层面的修改，因此使用其他模组（如 RoughlyEnoughItems）查看物品列表时，也会看到物品排列顺序被改变。

此外，本模组还提供修改物品所在物品组的功能。

本模组的 Fabric 版本**依赖 Fabric API 和 Cloth Config 模组**，如果不安装这些模组将无法运行。此外**建议安装 Mod Menu 模组**以进行配置。（已经安装了上述模组的不要重复安装。另外，部分模组，如 Bedrockify、Edit Sign、Better F3 可能会内置 Cloth Config 模组，这些情况下可以不再单独安装 Cloth Config。）

本模组的 Quilt 版本依赖 Quilt Standard Libraries，不依赖 Cloth Config，暂无配置界面，但是可以手动修改位于 `config/reasonable-sorting.json` 的配置文件并重启游戏。Minecraft 1.18.1 以下版本没有 Quilt Stand Libraries，故不依赖。如果 Quilt 版本出现问题，请尝试使用 Fabric 版本的模组代替。

自 2.0.0 版本开始，本模组不再与 1.5.2 以下的扩展方块形状（Extended Block Shapes）模组联动（当然也不会冲突）。请等待扩展方块形状模组的新版本。

注意：本模组正在开发 Forge 的版本。

## 配置

本模组可以通过 Mod Menu 进行配置。

### 排序

**启用排序**

默认为开启。如果关闭，所有的排序都将会按照原版进行，下面的这些配置也将失效。排序主要体现在创造模式物品栏以及 Roughly Enough Items 等模组（本模组不依赖）的物品列表中。

**启用默认物品排序规则**

默认为开启。本模组内置了一些物品排序规则，例如将冰、浮冰、蓝冰排在一起。

**自定义排序规则**

默认为空。你可以通过输入物品id来自定义一些排序规则。在模组配置界面中，点击左边的“+”号，下面将会出现一个文本框（不明显），在其中输入一条规则即可。其语法为：多个物品的id用空格隔开。例如，“`dirt white_wool diamond_block`”表示泥土、白色羊毛和钻石块将会排在一起，其中白色羊毛和钻石块将依次排在泥土后面。

**紧随基础方块的方块变种**

Minecraft 中，很多方块都具有其**变种**，例如橡木木板的“楼梯”变种为橡木楼梯，“台阶”变种为橡木台阶等等，也就是说，橡木木板是橡木楼梯、橡木台阶等方块的**基础方块**。你可以指定一些变种类型使之排在基础方块后面。

默认语法为多个方块变种名称用空格隔开。可用的方块变种名称会在模组配置界面显示。默认为 `stairs slab`，也就是说所有的楼梯、台阶会依次排在其基础方块后面。

需要注意的是，改变物品排序并不会改变物品所在的物品组。如要改变物品组，还需要设置**变种转移规则**。

**栅栏门紧随栅栏**

默认开启。将会使栅栏门方块紧跟在栅栏方块的后面。需要开启**栅栏门移至装饰性方块**，否则这些栅栏门仍会出现在“红石”物品组中，不会起到效果。

**仅限方块物品**

默认关闭。关闭时，无论是方块，还是物品形式的方块（也就是方块物品，其本质为物品），都会受到本模组的排序规则的影响。若开启，则只有方块物品会受影响，方块是不受影响的。也就是说，若开启，那么创造模式物品栏仍受本模组影响，但调试模式世界中的方块仍然按照原版排序。

### 物品组转移

**启用物品组转移**

默认为开启。如果关闭，所有的物品都会出现在原版物品组，下面的这些配置也将失效。

**按钮移至装饰性方块**

**栅栏门移至装饰性方块**

**剑移至工具**

**门移至装饰性方块**

以上四项设置的意思显而易见。其中，“栅栏门移至装饰性方块”默认为开启，其他的默认关闭。

**自定义物品转移规则**

默认为空。和自定义排序规则一样，一行一条规则，点击“+”新增一条规则。每条规则的语法是：物品id + 空格 + 需要移至的物品组。例如 `redstone_block building_blocks` 就会将红石块移至“建筑方块”（建材）物品组。

**自定义变种转移规则**

默认为空。和上面类似，语法是：变种名称 + 空格 + 需要移至的物品组。例如 `cut transportation` 就会将所有的切制方块移至“交通运输”物品组。

**自定义正则表达式转移规则**

默认为空。和上面类似，语法是：正则表达式 + 空格 + 需要移至的物品组。所有 id 符合该正则表达式的物品都会移至指定的物品组。正则表达式必须语法正确。例如 `.+?button transportation` 就会把所有 id 以 `button` 结尾的物品移至“交通运输”物品组。

## 技术细节

物品排序的实质是“指定一个领队物品和多个跟随物品，这些跟随物品将会跟随在领队物品的后面”。举个例子，对于规则 `dirt white_wool diamond_block`，泥土将会是领队物品，白色羊毛和钻石块将会跟随在泥土后面，而不再出现在原来的位置。

一个物品不能同时跟随多个物品，如果有，则只会跟随其中的一个。例如如果同时指定了 `dirt white_wool` 和 `grass_block white_wool` 两个规则，那么白色羊毛只会出现在泥土和草方块二者**其中一个**的后面。也就是说，物品**不会重复出现**。

物品**可以嵌套跟随**。例如，默认情况下，根据“紧随基础方块的方块变种”规则，橡木楼梯和橡木台阶会跟随在橡木木板后面，而根据“默认物品排序规则”，石化橡木台阶会跟随在橡木台阶后面。这样，物品栏中将会出现“橡木木板-橡木楼梯-橡木台阶-石化橡木台阶”的组合。

物品**不能够互相跟随、循环跟随**。例如，如果同时设置了 `dirt white_wool` 和 `white_wool dirt` 两条规则，则泥土和白色羊毛可能都不会出现（游戏日志中将会记录错误），有可能还会导致死循环。因此，应当避免这种情况。

关于物品组转移，转移后的物品将会不再出现在此物品组。但是，一个物品可以转移至多组。

## 开发

使用 `SortingRule.addSortingRule` 方法可以添加一条排序规则。使用 `SortingRule.addConditionalSortingRule` 可以添加一个只在特定条件下的排序规则。类似地，物品组转移规则可以使用 `TransferRule.addTransferRule` 或 `TransferRule.addConditionalTransferRule` 添加。