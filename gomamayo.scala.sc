//> using scala 3.3.0
//> using dep "com.worksap.nlp:sudachi:0.7.3"

object Epsilon

// PDAの遷移関数
def run0[S, I, Z](
    transition: Map[(S, I | Epsilon.type, Z), Set[(S, List[Z])]],
    q: S,
    sx: List[I | Epsilon.type],
    st: List[Z]
): Set[(S, List[I | Epsilon.type], List[Z])] = {
  val input = sx.head // handle empty at run
  val topStack = st.head
  val next = transition.get((q, input, topStack)).getOrElse(Set()) ++ transition
    .get((q, Epsilon, topStack))
    .getOrElse(Set())
  // println()
  // println(s"q: $q, input: $input, topStack: $topStack")
  // println(s"next: $next")
  next.map { (q2, topStackReplacement) =>
    val st2 = topStackReplacement ++ st.tail
    // println(s"stack: $st2")
    (q2, sx.tail, st2)
  }
}

// PDAの実行
def run[S, I, Z](
    transition: Map[(S, I | Epsilon.type, Z), Set[(S, List[Z])]],
    init: S,
    sx: List[I],
    // initialStackTop: Z,
    // 最初からスタックに積めるようにする
    initialStack: List[Z],
    acceptedStatus: Set[S]
): Set[(S, List[I | Epsilon.type], List[Z])] = {
  val initialState = (init, sx ++ List(Epsilon), initialStack)
  val finalState = initialState match {
    case (q, sx2, st) =>
      sx2.foldLeft[Set[(S, List[I | Epsilon.type], List[Z])]](
        Set((q, sx2, st))
      ) { (next, input) =>
        next.flatMap { n =>
          run0(transition, n._1, n._2, n._3)
        }
      }
  }

  // TODO: terminal epsilon search
  // println(s"finalState: $finalState")

  finalState.filter(s => acceptedStatus.contains(s._1))
}

def makeTransitionFromMora(
    mora: String
): Map[(Char, Char | Epsilon.type, Char), Set[(Char, List[Char])]] = {
  // スタックにはmoraの末尾がスタックのトップになるように積む
  // moraの先頭にマッチするまでは任意の文字を受け付ける
  // moraの先頭にマッチしたら、入力が空になるまで順にmoraにマッチし続けなければならない
  // moraの各文字に対して、スタックのトップが一致するかどうかを確認し、一致したらスタックからpopする
  // moraの各文字に対して、スタックのトップが一致しない場合は即座に失敗する
  // スタックにかかわらず、1文字でもマッチした状態で入力が空になったら受理する

  val endOfInput = '$'
  val acceptedState = '*'
  val stack = mora.reverse.toList
  val initialStackTop = stack.head.toChar

  val initialState = '^' // special state
  val initialTransition =
    Map[(Char, Char | Epsilon.type, Char), Set[(Char, List[Char])]](
      // 任意の文字を受け付ける
      ('^', Epsilon, initialStackTop) -> Set(('^', initialStackTop :: Nil))
    )
  val transition = ('^' +: stack).zip(stack).foldLeft(initialTransition) {
    case (acc, (prev, current)) =>
      acc ++ Map(
        // moraの先頭にマッチしたら、スタックをpopする
        (prev, current, current) -> Set((current, Nil))
      )
  }
  val restTransition =
    stack.zip(stack.tail).foldLeft(transition) { case (acc, (prev, current)) =>
      acc ++ Map(
        // スタックが1つでも消費されていて、入力が末尾に達したら受理する
        (prev, endOfInput, current) -> Set((acceptedState, current :: Nil)),
        // 一度受理状態になったら、受理状態でなんでも受け付ける
        (acceptedState, Epsilon, current) -> Set(
          (acceptedState, current :: Nil)
        )
      )
    }

  initialTransition ++ transition ++ restTransition
}

def checkGomamayo(mora: String, input: String): Option[Int] = {
  if (mora.isEmpty || input.isEmpty) {
    return None
  }
  // println(s"mora: $mora, input: $input")
  val moraTransition = makeTransitionFromMora(mora)
  val moraExtended = if (mora.length < input.length) {
    '/'.toString * (input.length - mora.length) ++ mora
  } else {
    mora
  }
  val result = run(
    moraTransition,
    '^',
    input.reverse.toList ++ List('$'),
    moraExtended.reverse.toList,
    Set('*')
  )
  if (result.isEmpty) {
    return None
  }
  // println(s"result: $result")
  val overwrappedLength =
    mora.length - result.map(_._3.filterNot(_ == '/').length).min
  // println(s"overwrappedLength: $overwrappedLength")
  Some(overwrappedLength)
}

def showCheckGomamayo(mora: String, input: String): String = {
  val result = checkGomamayo(mora, input)
  val message = if (result.isDefined) "ゴママヨ" else "非マヨ"
  s"$mora, $input => $message"
}

// println(showCheckGomamayo("ハクレイ", "レイム"))
// println(showCheckGomamayo("ギンコウ", "コウザ"))
// println(showCheckGomamayo("サイレンス", "スズカ"))
// println(showCheckGomamayo("ヤスダ", "ダイサーカス"))
// println(showCheckGomamayo("コウカイ", "カイツケ"))
// println(showCheckGomamayo("ブブン", "ブンスウ"))
// println(showCheckGomamayo("オレンジ", "レンジ"))
// println(showCheckGomamayo("ニコニ", "コウコク"))

import com.worksap.nlp.sudachi.{DictionaryFactory, Config, Tokenizer}
import java.nio.file.Paths
import collection.JavaConverters._
import io.AnsiColor._

val dictionaryFile = Paths.get("./system_core.dic");
val dictionary = new DictionaryFactory().create(
  Config.defaultConfig().systemDictionary(dictionaryFile)
);
val tokenizer = dictionary.create();
val tokenized =
  tokenizer.tokenize(Tokenizer.SplitMode.A, "公開買付")
// println(tokenized.asScala.toList.map(_.readingForm()))

def emphString(s: String): String = s"${RED}${s}${RESET}"

def showCheckGomamayoSudachi(sentence: String): String = {
  val tokenized = tokenizer.tokenize(Tokenizer.SplitMode.A, sentence)
  // println(tokenized.asScala.toList.map(_.readingForm()))
  val moras = tokenized.asScala.toList.zipWithIndex.map((t, i) =>
    (i, t.readingForm(), t.surface())
  )
  val result = moras
    .sliding(2)
    .flatMap { case List((i, mora1, sur1), (_, mora2, sur2)) =>
      checkGomamayo(mora1, mora2).map { overwrappedLength =>
        i -> (sur1, mora1, sur2, mora2, overwrappedLength)
      }
      case _ => List()
    }
    .toMap

  val message = moras
    .sliding(2)
    .toList
    .map { case List((i, mora1, sur1), (_, mora2, sur2)) =>
      result
        .get(i)
        .map { case (sur1, mora1, sur2, mora2, overwrappedLength) =>
          s"${emphString(sur1)}(${mora1.substring(0, mora1.length - overwrappedLength)}${emphString(
              mora1.substring(mora1.length - overwrappedLength)
            )})${emphString(sur2)}(${emphString(mora2.substring(0, overwrappedLength))}${mora2
              .substring(overwrappedLength)})"
        }
        .getOrElse(sur1)
      case _ => List()
    }
    .mkString

  message
}

println(showCheckGomamayoSudachi(args(0)))
