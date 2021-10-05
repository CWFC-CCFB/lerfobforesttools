# TODO: Add comment
# 
# Author: fortin
###############################################################################

path <- "./lerfob/test/lerfob/predictor/frenchcommercialvolume2014/"
speciesIndex <- read.csv(paste(path, "0_speciesIndex.csv", sep=""))
speciesIndex$latSp <- paste(speciesIndex$GENUS, speciesIndex$SPECIES)
speciesIndex <- speciesIndex[,c("CODE", "latSp", "match")]

dbhIndex <- read.csv(paste(path, "0_dbhIndex.csv", sep=""), dec=",")

fieldsToKeep <- c("Plot.ID.Number","Tree.Number","Stem.Diameter..cm.")

dbhIndex <- aggregate(dbhIndex[, fieldsToKeep], by=list(dbhIndex$Plot.ID.Number, dbhIndex$Tree.Number), FUN=mean)

dataSet <- read.csv(paste(path, "inventoryData.csv", sep=""), dec=",")

unique(dataSet$Tree.Species)

dataSetCompiled <- merge(dataSet, speciesIndex, by.x=c("Tree.Species"), by.y=c("CODE")) ## one observation plot 1 tree 3 has a wrong species code
dataSetCompiled <- merge(dataSetCompiled, dbhIndex[, fieldsToKeep], by=c("Plot.ID.Number","Tree.Number"))
View(dataSetCompiled)
dataToExport <- dataSetCompiled[c(fieldsToKeep, "latSp", "Total.tree.height", "match")]
colnames(dataToExport) <- c("PlotID", "TreeID", "dbhCm", "species", "htotM", "match")

write.csv(dataToExport, paste(path,"dataSet.csv",sep=""),row.names=FALSE)
unique(dataToExport$species)

missingData <- dataSet2[which(is.na(dataSet2$latSp)),]

dataSet3 <- merge

View(dataToExport)