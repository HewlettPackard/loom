plot_graph <- function(filename, data, x_value, main_label, xlab, ylab, ylim, legend, color) {
	png(filename)
	barplot(data, beside=TRUE, names.arg=x_value, main=main_label, xlab=xlab, ylab=ylab, ylim=ylim, legend=legend, col=color)
}

args <- commandArgs(TRUE)

metric = args[1]
date = args[2]
time = args[3]
path = args[4]
files <- args[5:length(args)]

x_value <- vector()

if (metric == "memory") {
	maximum <- vector()
	average <- vector()
} else if (metric == "cpu") {
		maximum_cpu_kernel <- vector()
		average_cpu_kernel <- vector()
		maximum_cpu_user_kernel <- vector()
		average_cpu__user_kernel <- vector()
		maximum_cpu_gc_kernel <- vector()
		average_cpu_gc_kernel <- vector()
}

for (file in files) {
	data = read.csv(file)
	x_value_len = length(x_value)
	if (x_value_len == 0) {
		x_value <- c(x_value, 10)
	} else {
		x_value <- c(x_value, (x_value[[length(x_value)]] * 10))
	}

	if (metric == "memory") {
		maximum <- c(maximum, max(data$Used.PS.Eden.Space..bytes.) / 1000000)
		average <- c(average, mean(data$Used.PS.Eden.Space..bytes.) / 1000000)
	} else if (metric == "cpu") {
		maximum_cpu_kernel <- c(maximum_cpu_kernel, max(data$CPU.time..kernel.....))
		average_cpu_kernel <- c(average_cpu_kernel, mean(data$CPU.time..kernel.....))
		maximum_cpu_user_kernel <- c(maximum_cpu_kernel, max(data$CPU.time..user...kernel.....))
		average_cpu__user_kernel <- c(average_cpu_kernel, mean(data$CPU.time..user...kernel.....))
		maximum_cpu_gc_kernel <- c(maximum_cpu_kernel, max(data$Time.spent.in.GC....))
		average_cpu_gc_kernel <- c(average_cpu_kernel, mean(data$Time.spent.in.GC....))
	}
}


if (metric == "memory") {
	max_value = max(max(maximum, average))
	memory_consumption <- rbind(maximum, average)
	filename = paste(path, "/memory.png", sep="")
	plot_graph(filename, memory_consumption, x_value, "Heap Memory Consumption", "Number of clients", "Megabytes", c(0, max_value + (max_value * 0.1)), c("Maximum", "Average"), c("red", "blue"))
} else if (metric == "cpu") {
	max_value = max(max(maximum_cpu_kernel, average_cpu_kernel))
	cpu_kernel_consumption <- rbind(maximum_cpu_kernel, average_cpu_kernel)
	filename = paste(path, "/cpu_kernel.png", sep="")
	plot_graph(filename, cpu_kernel_consumption, x_value, "CPU Kernel Consumption", "Number of clients", "Percent", c(0, max_value + (max_value * 0.1)), c("Maximum", "Average"), c("red", "blue"))

	max_value = max(max(maximum_cpu_user_kernel, average_cpu__user_kernel))
	cpu_user_kernel_consumption <- rbind(maximum_cpu_user_kernel, average_cpu__user_kernel)
	filename = paste(path, "/cpu_user_kernel.png", sep="")
	plot_graph(filename, cpu_kernel_consumption, x_value, "CPU User Kernel Consumption", "Number of clients", "Percent", c(0, max_value + (max_value * 0.1)), c("Maximum", "Average"), c("red", "blue"))

	max_value = max(max(maximum_cpu_gc_kernel, average_cpu_gc_kernel))
	cpu_gc_consumption <- rbind(maximum_cpu_gc_kernel, average_cpu_gc_kernel)
	filename = paste(path, "/cpu_gc.png", sep="")
	plot_graph(filename, cpu_kernel_consumption, x_value, "CPU GC Consumption", "Number of clients", "Percent", c(0, max_value + (max_value * 0.1)), c("Maximum", "Average"), c("red", "blue"))
}
