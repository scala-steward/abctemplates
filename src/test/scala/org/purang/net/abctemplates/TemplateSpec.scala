package org.purang.net.abctemplates

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


// Note: we test fragment merges only because for now that validates the underlying merge
@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements","org.wartremover.warts.ToString"))
class TemplateSpec extends AnyFlatSpec with Matchers
{

  "ABC Templates" should "allow fragment merge with a single key" in {
    val h: String =
      """|<div id="content" doh-content>
         |  <p>
         |    some imaginary data for instant visualization in the browser
         |  </p>
         |</div>
      """.stripMargin

    val template: Template = Template(h)

    val content: String = "<p>the real content!</p>"
    val attribute: String = "doh-content"
    val result: String = template.merge(Map("div[doh-content]" -> content))

    result should include(content)
    result should not include(attribute)
  }

  it should "allow fragment merge with a single key and line breaks" in {
    val h: String =
      """|<div id="content" doh-content>
         |  <p>
         |    some imaginary data for instant visualization in the browser
         |  </p>
         |</div>
      """.stripMargin

    val template: Template = Template(h)

    val content: String = "<p>the real \n content!</p>"
    val attribute: String = "doh-content"
    val result: String = template.merge(Map("div[doh-content]" -> content))

    result should include(content)
    result should not include(attribute)
  }

  it should "allow fragment merges with multiple keys" in {

    val h: String =
      """
        |<div class="entry">
        |  <h1 abc:title>some title</h1>
        |  <div class="body" abc:body>
        |    some body
        |  </div>
        |</div>
        |
      """.stripMargin

    val template: Template = Template(h)

    val title: String = "altered title"
    val body: String = "altered body"
    val result: String = template.merge(Map("[abc:title]" -> title, "[abc:body]" -> body))

    result should include(title)
    result should include(body)
    result should not include("abc:")
  }

  it should "allow fragment merges with attribute replacement" in {

    val h: String =
      """
        |  <link rel="stylesheet" type="text/css" media="print" href="stylesheets/print.css" abc:href/>
        |  <h1 abc:title>title </h1>
      """.stripMargin

    val template: Template = Template(h)

    val href: String = "stylesheets/print-sgshgshg.css"
    val title: String = "altered title"
    val result: String = template.merge(Map("a.[abc:href]" -> href, "[abc:title]" -> title))

    result should include(s"""href="$href"""")
    result should include(title)
    result should not include "abc:"
  }

  it should "allow fragment merges with attribute replacement and inner html replacement" in {

    val h: String =
      """
        |<a href="somelink.txt" abc:href abc:link-text>
        | some link text
        |</a>
      """.stripMargin

    val template: Template = Template(h)

    val href: String = "altered-link.txt"
    val text: String = "altered text"
    val result: String = template.merge(Map("a.[abc:href]" -> href, "[abc:link-text]" -> text))


    result should include(s"""href="$href"""")
    result should include(text)
    result should not include "abc:"
  }

  it should "allow for Nested expressions" in {
    sealed trait Sex
    case object Male extends Sex {
      override def toString = "M"
    }
    case object Female extends Sex {
      override def toString = "F"
    }

    case class Location(city: Vector[String])

    case class Person(fn: String, ln: String, sex: Sex, locs: Location)
    val ps: Vector[Person] = Vector(
      Person("GI", "Joe", Male, Location(Vector("sfo", "chi"))),
      Person("Dane", "Joe", Female, Location(Vector("nyc"))),
      Person("Baby", "Jane", Female, Location(Vector("lon")))
    )

    val h: String =
      """
        |<div class="bla">
        |  <div class="entry">
        |    <span abc:sex>M</span>
        |    <h1 abc:name>Max Musterman</h1>
        |    <div class="location">
        |      <ul abc:loc>
        |        <li>ber</li>
        |        <li>muc</li>
        |      </ul>
        |    </div>
        |  </div>
        |</div>
      """.stripMargin

    val li: String = """<li abc:loc-li>ber</li>"""

    val templateLi: Template = Template(li)
    def map(p: Person): Map[String, String] = Map(
      "[abc:sex]" -> p.sex.toString,
      "[abc:name]" -> (p.fn + " " + p.ln),
      "[abc:loc]" -> (for {l <- p.locs.city} yield templateLi.merge(Map("[abc:loc-li]" -> l))).mkString)

    val result: String = (for {
      i <- ps
      x = map(i)
      y = Template(h).merge(x)
    } yield y).mkString


    result should include("Dane Joe")
    result should include("Baby Jane")
    result should include("chi")
    result should include("nyc")
    result should include("lon")
    result should not include "abc:"
   }

  it should "allow for Block expressions" in {
   sealed trait Sex
   case object Male extends Sex {
    override def toString = "M"
   }
   case object Female extends Sex {
     override def toString = "F"
   }

   case class Location(city: Vector[String])

   case class Person(fn: String, ln: String, sex: Sex, locs: Location)
   val ps: Vector[Person] = Vector(
    Person("GI","Joe", Male, Location(Vector("sfo", "chi"))),
    Person("Dane","Joe", Female, Location(Vector("nyc"))),
    Person("Baby","Jane", Female, Location(Vector("lon")))
   )

    val h: String =
      """
        |<div class="entry">
        |  <span abc:sex>M</span>
        |  <h1 abc:name>Max Musterman</h1>
        |  <ul abc:loc>
        |    <li>ber</li>
        |    <li>muc</li>
        |  </ul>
        |</div>
        |
      """.stripMargin

    val li: String = """<li abc:loc>ber</li>"""

   val templateLi: Template = Template(li)
    def map(p: Person): Map[String, String] = Map(
     "[abc:sex]" -> p.sex.toString,
     "[abc:name]" -> (p.fn + " "+ p.ln),
     "[abc:loc]" -> (for {l <- p.locs.city } yield templateLi.merge(Map("[abc:loc]" -> l))).mkString)

   val result: String = (for {
      i <- ps
      x = map(i)
      y = Template(h).merge(x)
    } yield y).mkString

   result should include("Dane Joe")
   result should include("Baby Jane")
   result should include("chi")
   result should include("nyc")
   result should include("lon")
   result should not include "abc:"
  }

  it should "allow for some form of template and context reuse" in {
    //This is a contrived example .. templates and contextes should be simple
    sealed trait Sex
    case object Male extends Sex {
      override def toString = "M"
    }
    case object Female extends Sex {
      override def toString = "F"
    }

    case class Location(city: Vector[String])

    case class Person(fn: String, ln: String, sex: Sex, locs: Location)
    val ps: Vector[Person] = Vector(
      Person("GI", "Joe", Male, Location(Vector("sfo", "chi"))),
      Person("Dane", "Joe", Female, Location(Vector("nyc"))),
      Person("Baby", "Jane", Female, Location(Vector("lon")))
    )

    val container: String =
      """
        |<div class="entry" abc:container>
        | <span abc:sex>M</span>
        | <h1 abc:name>Max Musterman</h1>
        | <div class="location">
        |  <ul abc:loc>
        |    <li>ber</li>
        |    <li>muc</li>
        |  </ul>
        | </div>
        |</div>
      """.stripMargin

    val mini: String =
      """
        |<span abc:sex>M</span>
        |<h1 abc:name>Max Musterman</h1>
      """.stripMargin


    val more: String =
      """
        |<div class="location">
        | <ul abc:loc>
        |   <li>ber</li>
        |   <li>muc</li>
        | </ul>
        |</div>
      """.stripMargin

    val li: String = """<li abc:loc-li>ber</li>"""

    val tc: Template = Template(container)
    val tmini: Template = Template(mini)

    def miniM(p: Person): Map[String, String] = Map(
      "[abc:sex]" -> p.sex.toString,
      "[abc:name]" -> (p.fn + " " + p.ln))

    val tli: Template = Template(li)
    def moreM(p: Person): Map[String, String] = Map(
      "[abc:loc]" -> (for {l <- p.locs.city} yield tli.merge(Map("[abc:loc-li]" -> l))).mkString)

    val resultMini: String = (for {
      i <- ps
      x = miniM(i)
      y = Map("[abc:container]" -> tmini.merge(x))
    } yield tc.merge(y)).mkString

    val resultMore: String = (for {
      i <- ps
      tm = Template(mini + more)
      x = miniM(i) ++ moreM(i)
      y = Map("[abc:container]" -> tm.merge(x))
    } yield tc.merge(y)).mkString

    resultMini should include("Baby Jane")
    resultMini should not include "nyc"
    resultMore should include("nyc")
  }
}
