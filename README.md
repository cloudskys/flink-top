本项目主要用于实现一个"实时热门商品"的案例
	每隔5分钟统计一次最近一小时每个商品的点击量，所以窗口大小是一小时，每隔5分钟滑动一次。即分别要统计 [09:00, 10:00), [09:05, 10:05), [09:10, 10:10)… 等窗口的商品点击量。是一个常见的滑动窗口需求（Sliding Window）。