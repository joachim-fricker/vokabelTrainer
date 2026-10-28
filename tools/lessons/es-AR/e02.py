from _common import make
groups = [
("Zahlen", """
cero|cero|null|num
uno|uno, un, una|eins; ein, eine|num|Vor männlichen Nomen „un“: un peso, una hora.
dos|dos|zwei|num
tres|tres|drei|num
cuatro|cuatro|vier|num
cinco|cinco|fünf|num
seis|seis|sechs|num
siete|siete|sieben|num
ocho|ocho|acht|num
nueve|nueve|neun|num
diez|diez|zehn|num
once|once|elf|num
doce|doce|zwölf|num
quince|quince|fünfzehn|num
veinte|veinte|zwanzig|num
treinta|treinta|dreissig|num
cuarenta|cuarenta|vierzig|num
cincuenta|cincuenta|fünfzig|num
cien|cien|hundert|num|Ab 101: ciento uno, ciento cincuenta.
doscientos|doscientos, doscientas|zweihundert|num|Richtet sich nach dem Nomen: doscientas personas.
quinientos|quinientos|fünfhundert|num|Unregelmässig (nicht „cincocientos“).
mil|mil|tausend|num
millon|un millón|eine Million|num|Mit Nomen: un millón de pesos.
"""),
("Geld & Bezahlen", """
plata|la plata|das Geld|n|Wörtlich „Silber“; „dinero“ versteht man auch.
peso|el peso|der Peso|n
dolar|el dólar|der Dollar|n|Viele Preise (z. B. Unterkünfte) werden in Dollar genannt.
precio|el precio|der Preis|n
efectivo|el efectivo|das Bargeld|n|en efectivo = bar
tarjeta|la tarjeta|die Karte (Bank-/Kreditkarte)|n
billete|el billete|der Geldschein|n
moneda|la moneda|die Münze|n
vuelto|el vuelto|das Wechselgeld|n|Argentinisch; anderswo „el cambio“.
cajero|el cajero automático|der Geldautomat|n
caro|caro|teuer|adj
barato|barato|billig, günstig|adj
cuanto|¿cuánto?|wie viel?|adv
costar|costar|kosten|v|cuesta, cuestan
pagar|pagar|bezahlen|v
cambiar|cambiar|wechseln, umtauschen|v
descuento|el descuento|der Rabatt|n
"""),
("Uhrzeit & Tageszeiten", """
hora|la hora|die Stunde; die Uhrzeit|n
que_hora|¿qué hora es?|wie spät ist es?|ph
a_que_hora|¿a qué hora?|um wie viel Uhr?|ph
en_punto|en punto|Punkt, genau (Uhrzeit)|ph
y_media|y media|halb (nach der Stunde)|ph|las dos y media = halb drei
y_cuarto|y cuarto|Viertel nach|ph
menos_cuarto|menos cuarto|Viertel vor|ph
minuto|el minuto|die Minute|n
mediodia|el mediodía|der Mittag|n
medianoche|la medianoche|die Mitternacht|n
manana_m|la mañana|der Morgen, Vormittag|n|de la mañana = morgens; mañana ohne Artikel = morgen
tarde_n|la tarde|der Nachmittag|n
noche|la noche|die Nacht; der Abend|n
temprano|temprano|früh|adv
tarde|tarde|spät|adv
ahora|ahora|jetzt|adv
mas_o_menos|más o menos|ungefähr; mehr oder weniger|ph
abierto|abierto|offen, geöffnet|adj
cerrado|cerrado|geschlossen|adj
"""),
]
sentences = [
("¿Cuánto cuesta?","Wie viel kostet das?","cuanto costar"),
("¿Cuánto es?","Wie viel macht das?","cuanto"),
("Son veinte pesos.","Das macht zwanzig Pesos.","veinte peso"),
("Es muy caro.","Das ist sehr teuer.","caro"),
("Es barato.","Das ist günstig.","barato"),
("¿Puedo pagar con tarjeta?","Kann ich mit Karte bezahlen?","pagar tarjeta"),
("Solo efectivo, por favor.","Nur bar, bitte.","efectivo por_favor"),
("¿Hay descuento en efectivo?","Gibt es Rabatt bei Barzahlung?","descuento efectivo"),
("¿Dónde hay un cajero automático?","Wo gibt es einen Geldautomaten?","cajero uno"),
("No tengo plata.","Ich habe kein Geld.","no plata","negacion"),
("Necesito cambiar dólares.","Ich muss Dollar wechseln.","cambiar dolar"),
("¿Tenés vuelto de mil?","Hast du Wechselgeld für tausend?","vuelto mil","voseo"),
("Acá está el vuelto.","Hier ist das Wechselgeld.","vuelto"),
("Un billete de cien pesos.","Ein Hundert-Peso-Schein.","billete cien uno"),
("No tengo monedas.","Ich habe keine Münzen.","moneda no","negacion"),
("El precio es de quinientos pesos.","Der Preis beträgt fünfhundert Pesos.","precio quinientos"),
("Son doscientos pesos.","Das macht zweihundert Pesos.","doscientos","zahlen_geschlecht"),
("Cuesta un millón de pesos.","Es kostet eine Million Pesos.","costar millon","grosse_zahlen"),
("Tres mil quinientos.","Dreitausendfünfhundert.","tres mil quinientos","grosse_zahlen"),
("Una cerveza y dos empanadas.","Ein Bier und zwei Empanadas.","uno dos","zahlen_geschlecht"),
("Uno, dos, tres, cuatro, cinco.","Eins, zwei, drei, vier, fünf.","uno dos tres cuatro cinco"),
("Seis, siete, ocho, nueve, diez.","Sechs, sieben, acht, neun, zehn.","seis siete ocho nueve diez"),
("Mi número es cero, once, doce.","Meine Nummer ist null, elf, zwölf.","cero once doce"),
("Quince minutos.","Fünfzehn Minuten.","quince minuto"),
("Treinta, cuarenta, cincuenta.","Dreissig, vierzig, fünfzig.","treinta cuarenta cincuenta"),
("¿Qué hora es?","Wie spät ist es?","que_hora","uhrzeit"),
("Es la una.","Es ist ein Uhr.","uno","uhrzeit"),
("Son las dos en punto.","Es ist genau zwei Uhr.","dos en_punto","uhrzeit"),
("Son las tres y media.","Es ist halb vier.","tres y_media","uhrzeit"),
("Son las cuatro y cuarto.","Es ist Viertel nach vier.","cuatro y_cuarto","uhrzeit"),
("Son las ocho menos cuarto.","Es ist Viertel vor acht.","ocho menos_cuarto","uhrzeit"),
("¿A qué hora abre?","Um wie viel Uhr macht es auf?","a_que_hora","uhrzeit"),
("A las nueve de la mañana.","Um neun Uhr morgens.","nueve manana_m","uhrzeit"),
("A las cinco de la tarde.","Um fünf Uhr nachmittags.","cinco tarde_n","uhrzeit"),
("A las diez de la noche.","Um zehn Uhr abends.","diez noche","uhrzeit"),
("Al mediodía está cerrado.","Mittags ist geschlossen.","mediodia cerrado"),
("Está abierto hasta la medianoche.","Es ist bis Mitternacht geöffnet.","abierto medianoche"),
("Ahora está cerrado.","Jetzt ist geschlossen.","ahora cerrado"),
("Es temprano.","Es ist früh.","temprano"),
("Ya es muy tarde.","Es ist schon sehr spät.","tarde"),
("Una hora, más o menos.","Ungefähr eine Stunde.","hora mas_o_menos uno"),
("Llego en diez minutos.","Ich komme in zehn Minuten.","diez minuto"),
("¿Cuánto cuesta en dólares?","Wie viel kostet es in Dollar?","cuanto costar"),
("Pago yo.","Ich zahle.","pagar yo"),
("¿Me hacés un descuento?","Machst du mir einen Rabatt?","descuento","voseo"),
("La tarjeta no funciona.","Die Karte funktioniert nicht.","tarjeta no"),
("Son las doce y media.","Es ist halb eins.","doce y_media","uhrzeit"),
("El precio está en dólares.","Der Preis ist in Dollar angegeben.","precio"),
("Necesito una moneda de diez.","Ich brauche eine Zehner-Münze.","moneda diez"),
("¿A cuánto está el dólar hoy?","Wie steht der Dollar heute?","dolar"),
("Veinte mil pesos.","Zwanzigtausend Pesos.","veinte mil","grosse_zahlen"),
]
grammar = [
 {"id":"zahlen_geschlecht","title":"Zahlen mit Geschlecht",
  "explanation":"Die meisten Zahlen sind unveränderlich. Ausnahmen:\n\n• uno → vor männlichen Nomen un, vor weiblichen una: un peso, una hora\n• Hunderter ab 200 richten sich nach dem Nomen:\n  doscientos pesos – doscientas personas\n  trescientos, cuatrocientos, quinientos (!), seiscientos …\n\ncien steht allein oder direkt vor einem Nomen (cien pesos); ab 101 heisst es ciento: ciento veinte.",
  "examples":[{"text":"un café y una medialuna","de":"ein Kaffee und ein Hörnchen"},{"text":"doscientas cincuenta personas","de":"zweihundertfünfzig Personen"}]},
 {"id":"uhrzeit","title":"Die Uhrzeit",
  "explanation":"• Es la una. – Es ist ein Uhr. (Singular!)\n• Son las dos / las tres … – ab zwei Uhr Plural.\n\nMinuten:\n• y cuarto – Viertel nach\n• y media – halb (las dos y media = halb drei!)\n• menos cuarto – Viertel vor\n• en punto – Punkt\n\n„Um“ heisst a: a las ocho. Tageszeit: de la mañana / de la tarde / de la noche.",
  "examples":[{"text":"Son las siete y diez.","de":"Es ist zehn nach sieben."},{"text":"¿A qué hora sale? – A las once.","de":"Wann fährt er ab? – Um elf."}]},
 {"id":"grosse_zahlen","title":"Grosse Zahlen und Preise",
  "explanation":"• mil – tausend (ohne un!): mil pesos, dos mil, veinte mil\n• un millón – eine Million; vor einem Nomen mit de: un millón de pesos\n\nIn Argentinien sind Preise oft vierstellig oder höher. Statt Punkt schreibt man oft $ 3.500 (Tausenderpunkt).",
  "examples":[{"text":"Son cuatro mil pesos.","de":"Das macht viertausend Pesos."},{"text":"dos millones de personas","de":"zwei Millionen Menschen"}]},
]
wg = {"uno":["zahlen_geschlecht"],"doscientos":["zahlen_geschlecht"],"quinientos":["zahlen_geschlecht"],"mil":["grosse_zahlen"],"millon":["grosse_zahlen"],"hora":["uhrzeit"],"y_media":["uhrzeit"],"y_cuarto":["uhrzeit"],"menos_cuarto":["uhrzeit"]}
make('02_numeros_precios_hora.json','es-AR-02',2,'Zahlen, Preise & Uhrzeit','Números, precios y hora',groups,sentences,grammar,wg)
