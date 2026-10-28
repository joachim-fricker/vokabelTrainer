from _common import make
groups = [
("Geschäfte", """
negocio|el negocio|das Geschäft, der Laden|n
kiosco|el kiosco|der Kiosk|n|Getränke, Süsses, SUBE aufladen – an fast jeder Ecke.
almacen|el almacén|der kleine Lebensmittelladen|n
supermercado|el supermercado|der Supermarkt|n
verduleria|la verdulería|der Obst- und Gemüseladen|n
panaderia|la panadería|die Bäckerei|n
carniceria|la carnicería|die Metzgerei|n
feria|la feria|der Markt (Wochen-, Kunsthandwerkermarkt)|n
shopping|el shopping|das Einkaufszentrum|n|Argentinisch.
"""),
("Lebensmittel", """
fruta|la fruta|das Obst|n
manzana|la manzana|der Apfel|n|Bedeutet auch „Häuserblock“.
banana|la banana|die Banane|n
naranja|la naranja|die Orange|n
frutilla|la frutilla|die Erdbeere|n|Anderswo „fresa“.
durazno|el durazno|der Pfirsich|n|Anderswo „melocotón“.
palta|la palta|die Avocado|n|Anderswo „aguacate“.
tomate|el tomate|die Tomate|n
papa|la papa|die Kartoffel|n|Anderswo „patata“. Nicht verwechseln: el Papa = der Papst.
cebolla|la cebolla|die Zwiebel|n
manteca|la manteca|die Butter|n|Anderswo „mantequilla“.
yerba|la yerba|der Mate-Tee (Kraut)|n
galletitas|las galletitas|die Kekse|n
kilo|el kilo|das Kilo|n
medio|medio, media|halb|adj|medio kilo, media hora
"""),
("Kleidung", """
ropa|la ropa|die Kleidung|n
remera|la remera|das T-Shirt|n|Anderswo „camiseta“.
pantalon|el pantalón|die Hose|n
pollera|la pollera|der Rock|n|Anderswo „falda“.
campera|la campera|die Jacke|n|Anderswo „chaqueta“.
buzo|el buzo|der Pullover, das Sweatshirt|n
zapatillas|las zapatillas|die Turnschuhe|n
zapatos|los zapatos|die Schuhe|n
anteojos_de_sol|los anteojos de sol|die Sonnenbrille|n|Anderswo „gafas de sol“.
gorra|la gorra|die Schirmmütze|n
talle|el talle|die Grösse (Kleidung)|n|Anderswo „talla“.
probador|el probador|die Umkleidekabine|n
"""),
("Farben", """
color|el color|die Farbe|n
negro|negro|schwarz|adj
blanco|blanco|weiss|adj
rojo|rojo|rot|adj
azul|azul|blau|adj
verde|verde|grün|adj
amarillo|amarillo|gelb|adj
gris|gris|grau|adj
"""),
("Beschreiben & Vergleichen", """
grande|grande|gross|adj
chico|chico|klein|adj|In Argentinien meist „chico“ statt „pequeño“.
lindo|lindo|schön, hübsch|adj|Sehr häufig: ¡Qué lindo!
feo|feo|hässlich|adj
nuevo|nuevo|neu|adj
viejo|viejo|alt|adj
largo|largo|lang|adj|Achtung: nicht „large“ (gross)!
corto|corto|kurz|adj
mas|más|mehr|adv
menos|menos|weniger|adv
mejor|mejor|besser|adj
peor|peor|schlechter|adj
mucho|mucho, mucha|viel|adj
otro|otro, otra|ein anderer; noch ein|adj|Ohne „un“: otra remera.
este|este, esta|dieser, diese|pron
ese|ese, esa|der da, die da|pron
aquel|aquel, aquella|jener, jene (dort drüben)|pron
"""),
("Verben", """
comprar|comprar|kaufen|v
vender|vender|verkaufen|v
buscar|buscar|suchen|v
probar|probar|probieren; anprobieren|v|probarse = anprobieren; yo pruebo, vos probás
mirar|mirar|anschauen|v|Solo estoy mirando. = Ich schaue nur.
llevar|llevar|mitnehmen; nehmen (beim Kaufen)|v|Me lo llevo. = Ich nehme es.
bolsa|la bolsa|die Tüte|n
"""),
]
sentences = [
("¿Dónde hay un supermercado?","Wo gibt es einen Supermarkt?","supermercado donde hay"),
("Voy al kiosco a comprar agua.","Ich gehe zum Kiosk, Wasser kaufen.","kiosco comprar agua","del_al"),
("El almacén de la esquina abre temprano.","Der Laden an der Ecke macht früh auf.","almacen esquina temprano"),
("En la verdulería la fruta es más barata.","Im Obstladen ist das Obst günstiger.","verduleria fruta mas","comparativo"),
("Compro pan en la panadería.","Ich kaufe Brot in der Bäckerei.","pan panaderia comprar"),
("En la carnicería venden muy buena carne.","In der Metzgerei verkaufen sie sehr gutes Fleisch.","carniceria carne muy vender"),
("El domingo hay feria en la plaza.","Am Sonntag ist Markt auf dem Platz.","feria plaza hay"),
("Vamos al shopping.","Gehen wir ins Einkaufszentrum.","shopping ir","del_al"),
("Este negocio cierra a las ocho.","Dieser Laden schliesst um acht.","negocio este ocho","demostrativos"),
("Un kilo de manzanas, por favor.","Ein Kilo Äpfel, bitte.","kilo manzana por_favor"),
("Medio kilo de tomates.","Ein halbes Kilo Tomaten.","medio kilo tomate"),
("Dos bananas y una naranja.","Zwei Bananen und eine Orange.","banana naranja dos"),
("Un helado de frutilla.","Ein Erdbeereis.","frutilla helado"),
("Un jugo de durazno.","Ein Pfirsichsaft.","durazno jugo"),
("Una palta bien madura.","Eine schön reife Avocado.","palta"),
("Un kilo de papa y una cebolla.","Ein Kilo Kartoffeln und eine Zwiebel.","papa cebolla kilo"),
("Pan con manteca.","Brot mit Butter.","pan manteca con"),
("Medio kilo de yerba, por favor.","Ein halbes Kilo Yerba, bitte.","yerba medio kilo"),
("Un paquete de galletitas.","Eine Packung Kekse.","galletitas"),
("¿Me das una bolsa?","Gibst du mir eine Tüte?","bolsa","voseo"),
("Busco una remera blanca.","Ich suche ein weisses T-Shirt.","buscar remera blanco","adjetivos"),
("¿Tenés este pantalón en negro?","Hast du diese Hose in Schwarz?","pantalon este negro","demostrativos"),
("Esa pollera es muy linda.","Der Rock da ist sehr hübsch.","pollera ese lindo muy","demostrativos"),
("Necesito una campera para el frío.","Ich brauche eine Jacke für die Kälte.","campera necesitar"),
("Un buzo gris, talle M.","Ein grauer Pulli, Grösse M.","buzo gris talle"),
("¿Qué talle usás?","Welche Grösse trägst du?","talle que","voseo"),
("¿Puedo probarme estas zapatillas?","Kann ich diese Turnschuhe anprobieren?","probar zapatillas este","demostrativos"),
("¿Lo puedo probar?","Kann ich es anprobieren?","probar"),
("¿Dónde está el probador?","Wo ist die Umkleidekabine?","probador donde"),
("Estos zapatos son muy chicos.","Diese Schuhe sind zu klein.","zapatos chico muy este","adjetivos"),
("¿Tiene un talle más grande?","Haben Sie eine Nummer grösser?","talle mas grande","comparativo"),
("Necesito anteojos de sol.","Ich brauche eine Sonnenbrille.","anteojos_de_sol necesitar"),
("Una gorra azul.","Eine blaue Mütze.","gorra azul","adjetivos"),
("¿De qué color?","In welcher Farbe?","color que"),
("Me gusta el rojo.","Mir gefällt das Rote.","rojo"),
("El verde es más lindo que el amarillo.","Das Grüne ist schöner als das Gelbe.","verde amarillo mas lindo","comparativo"),
("Esta ropa es nueva.","Diese Kleidung ist neu.","ropa este nuevo","adjetivos"),
("Es un auto viejo, pero anda bien.","Es ist ein altes Auto, aber es läuft gut.","viejo auto"),
("Quiero algo nuevo.","Ich möchte etwas Neues.","nuevo querer"),
("Es muy feo.","Es ist sehr hässlich.","feo muy"),
("El pantalón es demasiado largo.","Die Hose ist zu lang.","pantalon largo"),
("Es un poco corto.","Es ist ein bisschen kurz.","corto un_poco"),
("¿Tenés otro color?","Hast du eine andere Farbe?","otro color","voseo"),
("Esta es mejor que aquella.","Diese ist besser als jene.","este mejor aquel","comparativo"),
("Este es peor.","Dieser ist schlechter.","este peor","comparativo"),
("Aquel negocio es más caro.","Jener Laden ist teurer.","aquel negocio mas caro","comparativo"),
("Cuesta menos en la feria.","Auf dem Markt kostet es weniger.","menos feria costar","comparativo"),
("Hay mucha gente en el shopping.","Im Einkaufszentrum sind viele Leute.","mucho shopping hay"),
("Solo estoy mirando, gracias.","Ich schaue nur, danke.","mirar gracias"),
("¿Puedo mirar?","Darf ich schauen?","mirar"),
("Voy a llevar esta remera.","Ich nehme dieses T-Shirt.","llevar remera este","ir_a"),
("¿Venden yerba acá?","Verkauft ihr hier Yerba?","vender yerba aca"),
("¿Tiene uno más chico?","Haben Sie eins in kleiner?","chico mas uno","comparativo"),
("Es lindo, pero muy caro.","Es ist schön, aber sehr teuer.","lindo caro muy"),
("Quiero comprar un regalo.","Ich möchte ein Geschenk kaufen.","comprar querer"),
("Tengo que buscar un regalo.","Ich muss ein Geschenk suchen.","buscar"),
("Un pantalón blanco.","Eine weisse Hose.","pantalon blanco"),
("Llevo una mochila grande.","Ich habe einen grossen Rucksack dabei.","mochila grande llevar"),
]
grammar = [
 {"id":"adjetivos","title":"Adjektive",
  "explanation":"Adjektive stehen meist nach dem Nomen und richten sich nach Geschlecht und Zahl:\n• un pantalón negro – una remera negra\n• los zapatos negros – las zapatillas negras\n\nAdjektive auf -e oder Konsonant haben keine weibliche Form: verde, grande, azul, gris.\nPlural: verdes, grandes, azules, grises.\n\n„Zu“ (zu gross) heisst demasiado oder in Argentinien oft einfach muy: muy chico.",
  "examples":[{"text":"una campera roja","de":"eine rote Jacke"},{"text":"dos remeras azules","de":"zwei blaue T-Shirts"}]},
 {"id":"demostrativos","title":"este, ese, aquel",
  "explanation":"Drei Stufen der Entfernung:\n• este / esta / estos / estas – dieser hier (bei mir)\n• ese / esa / esos / esas – der da (bei dir)\n• aquel / aquella / aquellos / aquellas – jener dort drüben\n\nOhne Nomen bedeuten sie „dieser/diese“: Quiero este. – Ich will diesen.\nNeutral (das hier): esto, eso, aquello – ¿Qué es eso?",
  "examples":[{"text":"Esta remera es linda.","de":"Dieses T-Shirt ist schön."},{"text":"¿Cuánto cuesta aquella campera?","de":"Wie viel kostet die Jacke dort drüben?"}]},
 {"id":"comparativo","title":"Vergleichen",
  "explanation":"• más … que – mehr … als: más barato que – günstiger als\n• menos … que – weniger … als: menos caro que\n• tan … como – so … wie: tan lindo como\n\nUnregelmässig:\n• bueno → mejor (besser)\n• malo → peor (schlechter)\n• grande → mayor (älter), chico → menor (jünger) – bei Grösse meist más grande / más chico.\n\nDer/die beste: el mejor, la mejor.",
  "examples":[{"text":"La feria es más barata que el shopping.","de":"Der Markt ist günstiger als das Einkaufszentrum."},{"text":"Este es el mejor.","de":"Das ist der beste."}]},
]
wg = {"este":["demostrativos"],"ese":["demostrativos"],"aquel":["demostrativos"],"mas":["comparativo"],"menos":["comparativo"],"mejor":["comparativo"],"peor":["comparativo"],"negro":["adjetivos"],"lindo":["adjetivos"]}
make('06_compras.json','es-AR-06',6,'Einkaufen','De compras',groups,sentences,grammar,wg)
