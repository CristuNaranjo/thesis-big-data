import java.io.{PrintWriter, FileWriter, File}
import java.sql.Timestamp
import org.apache.spark.ml.regression.{LinearRegressionModel, LinearRegression}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkContext, SparkConf}
import com.mongodb.spark._
import org.bson.Document
import org.apache.spark.sql.functions._

/**
  * Created by NaranjO.
  */
object SparkAnalysisGlobal {
  //Clases para quedarme solo con los puntos de los equipos

  case class Equipos(name: String, abbreviation: String, score: Int, home: Boolean, won: Int)

  case class Partido( date: Timestamp, teams: Equipos)


  val mongoConfig = new SparkConf()
  mongoConfig.setAppName("MongoSpark")
  mongoConfig.setMaster("local[4]")
  mongoConfig.set("spark.mongodb.input.uri", "mongodb://localhost:27017/NBA.games?readPreference=primaryPreferred")

  val sc = new SparkContext(mongoConfig)
  val sqlContext = SQLContext.getOrCreate(sc)
  val rdd = sc.loadFromMongoDB()
  rdd.cache()


  def prediccion(fecha: Array[String], abbrL: String, localPlayers: Array[String], abbrV: String, visitPlayers: Array[String]): Unit = {
    //Colecciono datos
    //println("Aqui en prediccion....")
    val resultados = resultadosPrevios(fecha,abbrL, abbrV, localPlayers, visitPlayers)
    val resultadosLocales = resultados._1
    val resultadosVisitantes = resultados._2
    resultadosLocales.cache()
    resultadosVisitantes.cache()
    //Locales
    var contL = 0
    val localesLP = resultadosLocales.map(puntos => {
      val LB = LabeledPoint(puntos.toDouble, Vectors.dense(contL))
      contL+=1
      (LB)
    })
    val parsedData = sqlContext.createDataFrame(localesLP)
    val linRegL = new LinearRegression().setMaxIter(100).setFitIntercept(true)
    var finalModelL = linRegL.fit(parsedData)
    val predL = finalModelL.transform(parsedData)
    var finalSummaryL = finalModelL.summary
    var i = 0
    while (i<100){
      val splits = parsedData.randomSplit(Array(0.7,0.3))
      val trainingData = splits(0)
      val evalData = splits(1)
      val model = linRegL.fit(trainingData)
      val predLM = model.transform(evalData)
      val trModel = model.summary
      if(trModel.rootMeanSquaredError < finalSummaryL.rootMeanSquaredError && i>0){
        finalModelL= model
        finalSummaryL = trModel
      }
      i+=1
    }
    val predictionLocal = finalModelL.intercept + finalModelL.coefficients(0)*parsedData.count()
    val ECML = finalSummaryL.rootMeanSquaredError
    val CR = finalSummaryL.r2
    val file = new File("/Users/NaranjO/Documents/TFG/MEAN/global.txt")
    val fw = new FileWriter(file);
    val pw = new PrintWriter(fw);
    pw.write("LOCAL\n")
    val localTest = resultadosLocales.collect()
    localTest.foreach(x=>pw.write(x.toString+"\n"))
    //resultadosLocales.take(resultadosLocales.count().toInt).foreach(x=>pw.write(x))
    //Visitantes
    var contV = 0
    val visitantesLP = resultadosVisitantes.map(puntos => {
      val LB = LabeledPoint(puntos.toDouble, Vectors.dense(contL))
      contL+=1
      (LB)
    })
    val parsedDataV = sqlContext.createDataFrame(visitantesLP)
    val linRegV = new LinearRegression().setMaxIter(100).setFitIntercept(true)
    var finalModelV = linRegV.fit(parsedDataV)
    val predV = finalModelV.transform(parsedDataV)
    var finalSummaryV = finalModelV.summary
    var x = 0
    while (x<100){
      val splits = parsedDataV.randomSplit(Array(0.7,0.3))
      val trainingDataV = splits(0)
      val evalDataV = splits(1)
      val model = linRegL.fit(trainingDataV)
      val predLM = model.transform(evalDataV)
      val trModel = model.summary
      if(trModel.rootMeanSquaredError < finalSummaryV.rootMeanSquaredError && i>0){
        finalModelV= model
        finalSummaryV = trModel
      }
      x+=1
    }
    pw.write("VISITANTES\n")
    val visitTest = resultadosVisitantes.collect()
    visitTest.foreach(x=>pw.write(x.toString+"\n"))
    val predictionVisitante = finalModelV.intercept + finalModelV.coefficients(0)*parsedDataV.count()
    val ECMV = finalSummaryV.rootMeanSquaredError
    val CRV = finalSummaryV.r2
    val dataLocal = (abbrL, predictionLocal, ECML,CR)
    val dataVisit = (abbrV,predictionVisitante,ECMV,CRV)
    pw.close

    writeResults(dataLocal,dataVisit)
  }


  def resultadosPrevios(fecha: Array[String],local: String, visitante: String, localPlayers: Array[String], visitPlayers: Array[String]): (RDD[(Int)], RDD[(Int)]) = {
    //Locales
    val rddEquipoL = SparkAnalysisGlobal.rdd.withPipeline(Seq(Document.parse("{$match: { 'teams.abbreviation': " + "'" + local + "'" + " }}"),
      Document.parse("{$unwind: '$teams'}"),
      Document.parse("{$match: { 'teams.abbreviation': " + "'" + local + "'" + " }}")))
    val dfEquipoL = rddEquipoL.toDF[Partido]
    dfEquipoL.cache()
    val resultadosLocales = dfEquipoL.select(dfEquipoL("teams.score")).map(puntos => puntos.getInt(0))

    //Visitantes
    val rddEquipoV = SparkAnalysisGlobal.rdd.withPipeline(Seq(Document.parse("{$match: { 'teams.abbreviation': " + "'" + visitante + "'" + " }}"),
      Document.parse("{$unwind: '$teams'}"),
      Document.parse("{$match: { 'teams.abbreviation': " + "'" + visitante + "'" + " }}")))
    val dfEquipoV = rddEquipoV.toDF[Partido]
    dfEquipoV.cache()
    val resultadosVisitantes = dfEquipoV.select(dfEquipoV("teams.score")).map(puntos => puntos.getInt(0))
    resultadosLocales.take(resultadosLocales.count().toInt).foreach(println)
    resultadosVisitantes.take(resultadosVisitantes.count().toInt).foreach(println)

    (resultadosLocales, resultadosVisitantes)
  }

  def writeResults(local:(String,Double,Double,Double) , visit:(String,Double,Double,Double) ): Unit = {
    val file = new File("/Users/NaranjO/Documents/TFG/MEAN/predictions.txt")
    val fw = new FileWriter(file);
    val pw = new PrintWriter(fw);
    val data = local._1 + "/" + local._2 +"/" +local._3 + "/" + local._4 +"\n"
    pw.write(data)
    val dataV = visit._1 + "/" + visit._2 +"/" +visit._3 + "/" + visit._4 +"\n"
    pw.write(dataV)
    pw.close()
  }

}
