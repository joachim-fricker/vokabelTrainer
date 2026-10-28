from _common import make
groups = [
("Im Restaurant", """
restaurante|el restaurante|das Restaurant|n
mesa|la mesa|der Tisch|n
mozo|el mozo|der Kellner|n|Kellnerin: la moza.
carta|la carta|die Speisekarte|n
menu|el menú|das Tagesmenü|n|menú del día = Mittagsmenü
cuenta|la cuenta|die Rechnung|n
propina|la propina|das Trinkgeld|n|Üblich sind etwa 10 %.
reserva|la reserva|die Reservierung|n
cubierto|el cubierto|das Gedeck|n|Wird in Restaurants oft extra berechnet.
plato|el plato|der Teller; das Gericht|n
vaso|el vaso|das Glas (Trinkglas)|n
taza|la taza|die Tasse|n
tenedor|el tenedor|die Gabel|n
cuchillo|el cuchillo|das Messer|n
cuchara|la cuchara|der Löffel|n
servilleta|la servilleta|die Serviette|n
pedir|pedir|bestellen; bitten|v|yo pido, vos pedís
querer|querer|wollen, möchten|v|yo quiero, vos querés
traer|traer|bringen|v|¿Me traés …? = Bringst du mir …?
recomendar|recomendar|empfehlen|v|yo recomiendo, vos recomendás
para_llevar|para llevar|zum Mitnehmen|ph
para_comer_aca|para comer acá|zum Hieressen|ph
"""),
("Essen", """
asado|el asado|das Asado (Grillfest, Grillfleisch)|n|Das argentinische Nationalgericht.
bife|el bife|das Steak|n|bife de chorizo = Rumpsteak, bife de lomo = Filet
carne|la carne|das Fleisch|n
pollo|el pollo|das Hähnchen|n
pescado|el pescado|der Fisch (als Essen)|n|Lebender Fisch: el pez.
milanesa|la milanesa|das panierte Schnitzel|n
empanada|la empanada|die Empanada (gefüllte Teigtasche)|n
choripan|el choripán|die Grillwurst im Brötchen|n
pizza|la pizza|die Pizza|n
ensalada|la ensalada|der Salat|n
papas_fritas|las papas fritas|die Pommes frites|n|la papa = die Kartoffel (nicht patata)
verdura|la verdura|das Gemüse|n
arroz|el arroz|der Reis|n
pan|el pan|das Brot|n
queso|el queso|der Käse|n
huevo|el huevo|das Ei|n
postre|el postre|der Nachtisch|n
helado|el helado|das Speiseeis|n
dulce_de_leche|el dulce de leche|die Milchkaramellcreme|n
medialuna|la medialuna|das Hörnchen (Croissant)|n
factura|la factura|das süsse Gebäck|n|Bedeutet auch „Rechnung (Beleg)“.
vegetariano|vegetariano|vegetarisch; Vegetarier|adj
"""),
("Getränke", """
agua|el agua|das Wasser|n:f|Weiblich, aber mit „el“: el agua fría.
agua_con_gas|agua con gas|das Sprudelwasser|ph
agua_sin_gas|agua sin gas|das stille Wasser|ph
cafe|el café|der Kaffee|n
cortado|el cortado|Espresso mit etwas Milch|n
cafe_con_leche|el café con leche|der Milchkaffee|n
te|el té|der Tee|n
mate|el mate|der Mate-Tee|n|Wird in der Runde aus einem Gefäss getrunken.
leche|la leche|die Milch|n
jugo|el jugo|der Saft|n|jugo de naranja = Orangensaft
gaseosa|la gaseosa|die Limonade, der Softdrink|n
cerveza|la cerveza|das Bier|n
vino|el vino|der Wein|n|vino tinto = Rotwein, vino blanco = Weisswein
hielo|el hielo|das Eis (Würfel)|n
azucar|el azúcar|der Zucker|n
botella|la botella|die Flasche|n
"""),
("Geschmack & Mahlzeiten", """
rico|rico|lecker|adj
caliente|caliente|heiss, warm|adj
frio|frío|kalt|adj
dulce|dulce|süss|adj
salado|salado|salzig|adj
picante|picante|scharf|adj
jugoso|jugoso|blutig, saftig (Fleisch)|adj
a_punto|a punto|medium (Fleisch)|ph
bien_cocido|bien cocido|durchgebraten|ph
sal|la sal|das Salz|n
hambre|el hambre|der Hunger|n:f|tener hambre = Hunger haben
sed|la sed|der Durst|n|tener sed = Durst haben
comer|comer|essen|v
tomar|tomar|trinken; nehmen|v|Für „trinken“ sagt man in Argentinien meist tomar.
desayuno|el desayuno|das Frühstück|n
almuerzo|el almuerzo|das Mittagessen|n
cena|la cena|das Abendessen|n|In Argentinien isst man oft erst ab 21 Uhr.
merienda|la merienda|die Nachmittagsmahlzeit|n|Gegen 17 Uhr: Mate oder Kaffee mit Gebäck.
muy|muy|sehr|adv
con|con|mit|prep
sin|sin|ohne|prep
"""),
]
sentences = [
("Una mesa para dos, por favor.","Einen Tisch für zwei, bitte.","mesa dos por_favor"),
("Tengo una reserva a las nueve.","Ich habe eine Reservierung um neun.","reserva nueve"),
("¿Me traés la carta, por favor?","Bringst du mir bitte die Speisekarte?","carta traer por_favor","pedir_comida"),
("¿Qué plato me recomendás?","Welches Gericht empfiehlst du mir?","plato recomendar","voseo"),
("¿Tienen menú del día?","Habt ihr ein Tagesmenü?","menu"),
("Quiero un bife de chorizo jugoso.","Ich möchte ein Rumpsteak, blutig.","querer bife jugoso","pedir_comida"),
("Para mí, una milanesa con papas fritas.","Für mich ein Schnitzel mit Pommes.","milanesa con papas_fritas","pedir_comida"),
("Dos empanadas de carne, por favor.","Zwei Empanadas mit Fleisch, bitte.","empanada carne dos","plural"),
("El bife bien cocido, por favor.","Das Steak bitte durchgebraten.","bife bien_cocido","articulos"),
("A punto, por favor.","Medium, bitte.","a_punto"),
("Soy vegetariano.","Ich bin Vegetarier.","vegetariano"),
("¿Tienen un plato vegetariano?","Habt ihr ein vegetarisches Gericht?","plato vegetariano"),
("Una ensalada sin cebolla.","Einen Salat ohne Zwiebeln.","ensalada sin"),
("Pollo con arroz.","Hähnchen mit Reis.","pollo con arroz"),
("El pescado está muy rico.","Der Fisch ist sehr lecker.","pescado muy rico"),
("¿Me traés un poco de pan?","Bringst du mir etwas Brot?","traer pan un_poco","pedir_comida"),
("Un choripán y una gaseosa.","Ein Choripán und eine Limo.","choripan gaseosa uno"),
("Una pizza para llevar.","Eine Pizza zum Mitnehmen.","pizza para_llevar"),
("¿Para comer acá o para llevar?","Zum Hieressen oder zum Mitnehmen?","para_comer_aca para_llevar"),
("El domingo hacemos un asado.","Am Sonntag machen wir ein Asado.","asado"),
("No como carne.","Ich esse kein Fleisch.","comer carne no","negacion"),
("Tengo mucha hambre.","Ich habe grossen Hunger.","hambre","tener_hambre"),
("Tengo sed.","Ich habe Durst.","sed","tener_hambre"),
("Un agua sin gas, por favor.","Ein stilles Wasser, bitte.","agua_sin_gas","articulos"),
("Una botella de agua con gas.","Eine Flasche Sprudelwasser.","botella agua_con_gas"),
("Un café con leche y dos medialunas.","Einen Milchkaffee und zwei Hörnchen.","cafe_con_leche medialuna dos","plural"),
("Un cortado, por favor.","Einen Cortado, bitte.","cortado"),
("¿Tomás mate?","Trinkst du Mate?","tomar mate","voseo"),
("Un té con leche, sin azúcar.","Einen Tee mit Milch, ohne Zucker.","te leche sin azucar"),
("Un jugo de naranja.","Einen Orangensaft.","jugo"),
("Una cerveza bien fría.","Ein schön kaltes Bier.","cerveza frio"),
("Una copa de vino tinto.","Ein Glas Rotwein.","vino"),
("Con hielo, por favor.","Mit Eis, bitte.","con hielo"),
("¿Me das un vaso de agua?","Gibst du mir ein Glas Wasser?","vaso agua","voseo"),
("Falta un tenedor.","Es fehlt eine Gabel.","tenedor"),
("¿Me traés otro cuchillo?","Bringst du mir ein anderes Messer?","cuchillo traer","pedir_comida"),
("Necesito una cuchara.","Ich brauche einen Löffel.","cuchara"),
("Una servilleta, por favor.","Eine Serviette, bitte.","servilleta por_favor"),
("Una taza de café.","Eine Tasse Kaffee.","taza cafe"),
("El café está frío.","Der Kaffee ist kalt.","cafe frio"),
("La comida está muy caliente.","Das Essen ist sehr heiss.","caliente muy"),
("¿Es picante?","Ist das scharf?","picante"),
("Está un poco salado.","Es ist ein bisschen salzig.","salado un_poco"),
("¿Me pasás la sal?","Gibst du mir das Salz?","sal","voseo"),
("De postre, un helado de dulce de leche.","Als Nachtisch ein Dulce-de-leche-Eis.","postre helado dulce_de_leche"),
("El flan es muy dulce.","Der Pudding ist sehr süss.","dulce muy"),
("Una factura con dulce de leche.","Ein Gebäckstück mit Dulce de leche.","factura dulce_de_leche"),
("Un huevo frito, por favor.","Ein Spiegelei, bitte.","huevo"),
("Una pizza con mucho queso.","Eine Pizza mit viel Käse.","pizza queso"),
("Con verdura, sin carne.","Mit Gemüse, ohne Fleisch.","verdura carne con sin"),
("La cuenta, por favor.","Die Rechnung, bitte.","cuenta por_favor","articulos"),
("¿La propina está incluida?","Ist das Trinkgeld inbegriffen?","propina"),
("¿Cuánto es el cubierto?","Wie viel kostet das Gedeck?","cubierto cuanto"),
("¡Mozo! ¿Me cobra, por favor?","Herr Ober! Kann ich bitte zahlen?","mozo"),
("Queremos pedir.","Wir möchten bestellen.","pedir querer","pedir_comida"),
("¿Qué querés comer?","Was willst du essen?","querer comer","voseo"),
("¿Qué vas a tomar?","Was trinkst du?","tomar"),
("El desayuno es a las ocho.","Das Frühstück ist um acht.","desayuno ocho"),
("El almuerzo es al mediodía.","Das Mittagessen ist mittags.","almuerzo mediodia"),
("En Argentina la cena es muy tarde.","In Argentinien ist das Abendessen sehr spät.","cena muy tarde"),
("A la tarde tomamos la merienda.","Nachmittags gibt es die Merienda.","merienda tomar"),
("Este restaurante es muy bueno.","Dieses Restaurant ist sehr gut.","restaurante muy"),
("El plato es enorme.","Das Gericht ist riesig.","plato"),
("Las empanadas están muy ricas.","Die Empanadas sind sehr lecker.","empanada muy rico","plural"),
("Dos cafés y tres cervezas.","Zwei Kaffee und drei Bier.","cafe cerveza dos tres","plural"),
("Quiero pescado, no pollo.","Ich möchte Fisch, kein Hähnchen.","pescado pollo querer"),
]
grammar = [
 {"id":"articulos","title":"Artikel: el, la, los, las",
  "explanation":"Bestimmter Artikel:\n• el (m) – el plato, el café\n• la (f) – la mesa, la cuenta\n• los / las (Plural) – los platos, las mesas\n\nUnbestimmt: un (m), una (f), unos/unas (einige).\n\nBesonderheit: Weibliche Wörter, die mit betontem a beginnen, nehmen el: el agua, el hambre – bleiben aber weiblich: el agua fría.\n\nde + el = del, a + el = al: el menú del día, al mediodía.",
  "examples":[{"text":"el vino y la cerveza","de":"der Wein und das Bier"},{"text":"el agua está fría","de":"das Wasser ist kalt"}]},
 {"id":"pedir_comida","title":"Bestellen",
  "explanation":"Die üblichen Wendungen:\n• Quiero … – Ich möchte …\n• Para mí, … – Für mich …\n• ¿Me traés …? – Bringst du mir …? (zu usted: ¿Me trae …?)\n• ¿Me das …? – Gibst du mir …?\n• ¿Me cobrás? – Kann ich zahlen? (wörtl. Kassierst du bei mir?)\n\nquerer: yo quiero, vos querés, usted quiere, nosotros queremos.",
  "examples":[{"text":"Para mí, un cortado.","de":"Für mich einen Cortado."},{"text":"¿Me traés la cuenta?","de":"Bringst du mir die Rechnung?"}]},
 {"id":"tener_hambre","title":"tener hambre, tener sed …",
  "explanation":"Viele Zustände drückt man mit tener (haben) + Nomen aus:\n• tener hambre – Hunger haben\n• tener sed – Durst haben\n• tener frío / calor – frieren / schwitzen\n• tener sueño – müde sein\n\ntener: yo tengo, vos tenés, usted tiene, nosotros tenemos, ustedes tienen.\n„Sehr“ heisst hier mucho/mucha: Tengo mucha hambre.",
  "examples":[{"text":"¿Tenés hambre?","de":"Hast du Hunger?"},{"text":"Tenemos mucho calor.","de":"Uns ist sehr heiss."}]},
 {"id":"plural","title":"Mehrzahl",
  "explanation":"• Endet ein Wort auf Vokal: + s – la empanada → las empanadas, el café → los cafés\n• Endet es auf Konsonant: + es – el pan → los panes, el bife → los bifes, la ciudad → las ciudades\n• -z wird zu -ces: el lápiz → los lápices\n\nAdjektive passen sich an: las empanadas ricas.",
  "examples":[{"text":"dos cervezas y tres empanadas","de":"zwei Bier und drei Empanadas"},{"text":"los panes","de":"die Brote"}]},
]
wg = {"agua":["articulos"],"hambre":["articulos","tener_hambre"],"sed":["tener_hambre"],"querer":["pedir_comida"],"pedir":["pedir_comida"],"traer":["pedir_comida"],"empanada":["plural"]}
make('03_restaurante_cafe.json','es-AR-03',3,'Restaurant & Café','Restaurante y café',groups,sentences,grammar,wg)
