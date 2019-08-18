
############ Extracting dataset 2: GSE62646 ############
geo_id = 'GSE62646'
data <- getGEO(geo_id, GSEMatrix=TRUE)

# Extract gene expression data
data.exp <- as.data.frame(exprs(data[[1]]))

# Format to 5 decimals
data.exp=format(as.data.frame(data.exp), digits=5)


#############
# Annotation
#############
# Which annotation file to use?
get_annotation_db("GPL6244")

biocLite("hugene10sttranscriptcluster.db")
library(hugene10sttranscriptcluster.db)

probe_ids = rownames(data.exp)
gene_symbols = unlist(mget(probe_ids, hugene10sttranscriptclusterSYMBOL, ifnotfound=NA))
annotated = as.data.frame(cbind(probe_ids, gene_symbols))

################
# IQR filtering
################
# Merging annotation with expression data
data.exp$probe_ids <- rownames(data.exp)
data.annotated = merge(data.exp, annotated, by.x="probe_ids", by.y="probe_ids")
write.table(data.annotated, paste(geo_id,"_exp_annotated.txt", sep=""),sep="\t",row.names=F)
data.annotated = read.delim(paste(geo_id,"_exp_annotated.txt", sep=""),sep="\t",check.names=F)


# Sorting by gene symbols
data.annotated.sorted = data.annotated[order(data.annotated$gene_symbols),]
logdata = data.annotated.sorted[,!(colnames(data.annotated.sorted) %in% c("probe_ids", "gene_symbols"))]
unlogdata = 2^logdata


# Calculating IQR for all probes using unlog data
iqr <- apply(unlogdata,1,IQR)
data.iqr = cbind(data.annotated.sorted[,(colnames(data.annotated.sorted) %in% c("probe_ids", "gene_symbols"))], iqr, unlogdata)
write.table((data.iqr), paste(geo_id,"_unlog.IQR.txt", sep=""), sep="\t",row.names=F)


# Keep probe with highest iqr in case of multiple probes
names(iqr) = data.annotated.sorted$probe_ids
iqrs = split.default(iqr, data.annotated.sorted$gene_symbols)
maxes = sapply(iqrs, function(x) names(which.max(x)))
singleprobe = data.iqr[data.iqr$probe_ids %in% maxes, !(colnames(data.iqr) == "probe_ids")]


## remove row with gene symbol NA
newdata = singleprobe
write.table(newdata, paste(geo_id,"_singleprobe_unlogged.txt", sep=""),sep="\t", row.names=FALSE,quote=FALSE)

d = newdata[,!(colnames(newdata) %in% c("gene_symbols", "iqr"))]
gene_symbols <- newdata[,(colnames(newdata) %in% c("gene_symbols"))]
logd = cbind(gene_symbols, log2(d))
logd[mapply(is.infinite, logd)] <- 1024.0
write.table(logd, paste(geo_id,"_singleprobe_logged.txt", sep=""),sep="\t", row.names=FALSE,quote=FALSE)


#Set column names to gene
rownames(logd) <- logd[,1]

# Drop column "gene_symbols"
logd <- logd[,-which(names(logd) %in% c("gene_symbols"))]


# Check if column names are unique
length(rownames(logd))
length(unique(rownames(logd)))

# if not unique, make unique
rownames(logd) = make.unique(rownames(logd), sep="_")


# Transpose
logd = t(logd)




#############
# Phenotype
#############
names(pData(data[[1]]))

# ID and Target variable column
data.pheno <- as.data.frame(pData(data[[1]])[, c(2,35,37)])
names(data.pheno)[1]<-"#Identifier"
names(data.pheno)[2]<-"@Class"
names(data.pheno)[3]<-"Group"

# Edit outcome values to Case/Normal
View(data.pheno)
data.pheno$"@Class" <- gsub('STEMI', 'Case', data.pheno$"@Class")
data.pheno$"@Class" <- gsub('CAD', 'Normal', data.pheno$"@Class")

# Merge expression data with phenotype.
data.all <- cbind(data.pheno, logd)

# Only keep instances measured during admission.
data.all <- data.all[grep("^admission",data.pheno$Group),]
data.all <- subset(data.all, select = -c(Group))

# Write data to file
write.table(data.all, paste(geo_id,"_exp.csv", sep = ""),sep=",",row.names=F, quote = FALSE)


