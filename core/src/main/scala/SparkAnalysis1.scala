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
object SparkAnalysis1 {
  //Clases para quedarme solo con los puntos de cada jugador
  case class Jugadores(pts: Int)

  case class Box(players: Jugadores, won: Int)

  case class Equipos(name: String, abbreviation: String, score: Int, home: Boolean, won: Int)

  case class Partido(box: Box, date: Timestamp, teams: Array[Equipos])


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
    val localesAgrupados = resultadosLocales.groupByKey()
    //.groupByKey()
    val localesLP = localesAgrupados.map(tuple => {
      val player = tuple._1
      val pts = tuple._2
      var cont = 0.0
      var arrayLBPts: Array[LabeledPoint] = Array()
      for (elem <- pts){
        val lpPts = LabeledPoint(elem.toDouble, Vectors.dense(cont))
        arrayLBPts :+= lpPts
        cont+=1
      }
      val dataFrame = sqlContext.createDataFrame(arrayLBPts)
      (player,dataFrame)
    })
    val dataModelLocal = localesLP.map(tuple => {
      val player = tuple._1
      val parsedData = tuple._2
      var i = 0
      val linReg = new LinearRegression().setMaxIter(100).setFitIntercept(true)
      var finalModel = linReg.fit(parsedData)
      var finalSummary = finalModel.summary

      while(i<100){
        val splits = parsedData.randomSplit(Array(0.7,0.3))
        // Building the model
        val trainingData = splits(0)
        val evalData = splits(1)
        val model = linReg.fit(trainingData)
        val trModel = model.summary
        if(trModel.rootMeanSquaredError < finalSummary.rootMeanSquaredError && i>0){
          finalModel= model
          finalSummary = trModel
        }
        i+=1
      }
      /*
      val splits = parsedData.randomSplit(Array(0.7,0.3))
      // Building the model
      val trainingData = splits(0)
      val evalData = splits(1)
      val linReg = new LinearRegression().setMaxIter(100).setFitIntercept(true)
      val model = linReg.fit(trainingData)
      */
      // val model = linReg.fit(parsedData)
      /*(player, parsedData, finalModel, evalData)*/
      (player, parsedData, finalModel, parsedData)
    })
    val dataPredictionLocal = dataModelLocal.map(tuple=>{
      val player = tuple._1
      val parsedData = tuple._2
      val model = tuple._3
      val evalData = tuple._4
      val fullPredict = model.transform(evalData)
      val trainingSummary = model.summary
      val prediction = model.intercept + model.coefficients(0)*parsedData.count()
      val ECM = trainingSummary.rootMeanSquaredError
      val CR = trainingSummary.r2

      (player,model.intercept, model.coefficients(0), prediction)
    })

    //Visitantes

    val visitantesAgrupados = resultadosVisitantes.groupByKey()
    val visitantesLP = visitantesAgrupados.map(tuple => {
      val player = tuple._1
      val pts = tuple._2
      var cont = 0.0
      var arrayLBPts: Array[LabeledPoint] = Array()
      for (elem <- pts){
        val lpPts = LabeledPoint(elem.toDouble, Vectors.dense(cont))
        arrayLBPts :+= lpPts
        cont+=1
      }
      val dataFrame = sqlContext.createDataFrame(arrayLBPts)
      (player,dataFrame)
    })
    val dataModelVisitante = visitantesLP.map(tuple => {
      val player = tuple._1
      val parsedData = tuple._2
      var i = 0
      val linReg = new LinearRegression().setMaxIter(100).setFitIntercept(true)
      var finalModel = linReg.fit(parsedData)
      var finalSummary = finalModel.summary

      while (i < 100) {
        val splits = parsedData.randomSplit(Array(0.7, 0.3))
        // Building the model
        val trainingData = splits(0)
        val evalData = splits(1)
        val model = linReg.fit(trainingData)
        val trModel = model.summary
        if (trModel.rootMeanSquaredError < finalSummary.rootMeanSquaredError && i > 0) {
          finalModel = model
          finalSummary = trModel
        }
        i += 1
      }
      (player, parsedData, finalModel, parsedData)
    })
    val dataPredictionVisitante = dataModelVisitante.map(tuple=>{
      val player = tuple._1
      val parsedData = tuple._2
      val model = tuple._3
      val evalData = tuple._4
      val fullPredict = model.transform(parsedData)
      val trainingSummary = model.summary
      val prediction = model.intercept + model.coefficients(0)*parsedData.count()
      val ECM = trainingSummary.rootMeanSquaredError
      val CR = trainingSummary.r2
      (player, model.intercept, model.coefficients(0), prediction)
    })

    writeResults(dataPredictionLocal,dataPredictionVisitante)
  }


  def resultadosPrevios(fecha: Array[String],local: String, visitante: String, localPlayers: Array[String], visitPlayers: Array[String]): (RDD[(String, Int)], RDD[(String, Int)]) = {
    var totalLocalResults: Array[(String, Int)] = Array()
    var totalVisitResults: Array[(String,Int)] = Array()

    //Filtrar mongoDB para obtener los resultados previos entre cada jugador y el equipo rival
    //Primero filtro para obtener los partidos del jugador
    //Locales
    for (i <- 0 until localPlayers.length) {
      var puntos: Array[Int] = Array()
      var player = localPlayers(i)
      println(player)
      val rddJugador = SparkAnalysis1.rdd.withPipeline(Seq(Document.parse("{$match: { 'box.players.player': " + "'" + player + "'" + " }}"),
        Document.parse("{$unwind: '$box'}"),
        Document.parse("{$match: {'box.players.player':" + "'" + player + "'" + "}}"),
        Document.parse("{$unwind: '$box.players'}"),
        Document.parse("{$match: { 'box.players.player': " + "'" + player + "'" + " }}")))
      val dfJugador = rddJugador.toDF[Partido]
      dfJugador.cache()
      val resultadosLocales = dfJugador.select(dfJugador("box.players.pts"))
        .map(puntos => (player,puntos.getInt(0)))
          //.map(puntos => (player,puntos.getInt(0),player))
      //resultadosLocales.take(resultadosLocales.count().toInt).foreach(println)
      totalLocalResults = totalLocalResults ++ resultadosLocales.collect()
    }
    //Visitantes
    for (i <- 0 until visitPlayers.length) {
      var puntos: Array[Int] = Array()
      var player = visitPlayers(i)
      //println(player)
      val rddJugador = SparkAnalysis1.rdd.withPipeline(Seq(Document.parse("{$match: { 'box.players.player': " + "'" + player + "'" + " }}"),
        Document.parse("{$unwind: '$box'}"),
        Document.parse("{$match: {'box.players.player':" + "'" + player + "'" + "}}"),
        Document.parse("{$unwind: '$box.players'}"),
        Document.parse("{$match: { 'box.players.player': " + "'" + player + "'" + " }}")))
      val dfJugador = rddJugador.toDF[Partido]
      dfJugador.cache()
      val resultadosVisitantes = dfJugador.select(dfJugador("box.players.pts"))
          .map(puntos=> (player,puntos.getInt(0)))
        //.map(puntos => (player,puntos.getInt(0),player))
      //resultadosVisitantes.take(resultadosVisitantes.count().toInt).foreach(println)
      totalVisitResults = totalVisitResults ++ resultadosVisitantes.collect()
    }
    val localReturn = sc.parallelize(totalLocalResults)
    val visitReturn = sc.parallelize(totalVisitResults)
    (localReturn, visitReturn)
  }

  def writeResults(local:RDD[(String,Double,Double,Double)] , visit:RDD[(String,Double,Double,Double)] ): Unit = {
    val file = new File("/Users/NaranjO/Documents/TFG/MEAN/predictions.txt")
    val fw = new FileWriter(file);
    val pw = new PrintWriter(fw);
    local.take(20).foreach(tuple => {
      val player = tuple._1
      val ECM = tuple._2
      val R2 = tuple._3
      val pts = tuple._4
      //val data = player + "//" + ECM.toString + "//" + R2.toString + "//" + pts.toString + "\n"
      val data = player + "/"  + pts.toString + "\n"
      pw.write(data)
    })
    pw.write("\n")
    visit.take(20).foreach(tuple => {
      val player = tuple._1
      val ECM = tuple._2
      val R2 = tuple._3
      val pts = tuple._4
      val data = player + "/"  + pts.toString + "\n"
      //val data = player + "//" + ECM.toString + "//" + R2.toString + "//" + pts.toString + "\n"
      pw.write(data)
    })
    pw.close()
  }

}
