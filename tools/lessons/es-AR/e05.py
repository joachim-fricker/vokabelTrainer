from _common import make
groups = [
("Verkehrsmittel", """
colectivo|el colectivo|der Stadtbus|n|Argentinisch! Umgangssprachlich: el bondi.
micro|el micro|der Fernbus|n|Lange Strecken fährt man in Argentinien meist mit dem Bus.
subte|el subte|die U-Bahn (Buenos Aires)|n
tren|el tren|der Zug|n
avion|el avión|das Flugzeug|n
taxi|el taxi|das Taxi|n
remis|el remís|das bestellte Auto mit Fahrer|n|Wird vorab bestellt, Preis vorher fest.
auto|el auto|das Auto|n|In Argentinien nicht „coche“.
bicicleta|la bicicleta|das Fahrrad|n
barco|el barco|das Schiff|n|Nach Uruguay (Colonia) fährt eine Fähre.
a_pie|a pie|zu Fuss|ph
"""),
("Reisen & Fahrkarten", """
aeropuerto|el aeropuerto|der Flughafen|n
terminal|la terminal|der Busbahnhof|n|Ganz: la terminal de ómnibus.
estacion|la estación|der Bahnhof; die Station|n
parada|la parada|die Haltestelle|n
pasaje|el pasaje|die Fahrkarte, das Ticket|n|Für Fernbus und Flug.
boleto|el boleto|der Fahrschein (Stadtverkehr)|n
sube|la SUBE|die Karte für Bus und U-Bahn|n|Aufladbare Karte; im Kiosk erhältlich.
ida_y_vuelta|ida y vuelta|hin und zurück|ph
vuelo|el vuelo|der Flug|n
valija|la valija|der Koffer|n|Argentinisch; anderswo „maleta“.
mochila|la mochila|der Rucksack|n
equipaje|el equipaje|das Gepäck|n
salida|la salida|die Abfahrt; der Ausgang|n
llegada|la llegada|die Ankunft|n
demora|la demora|die Verspätung|n|Anderswo „retraso“.
asiento|el asiento|der Sitzplatz|n
"""),
("Orientierung", """
calle|la calle|die Strasse|n
avenida|la avenida|die Hauptstrasse, Allee|n
cuadra|la cuadra|der Häuserblock (ca. 100 m)|n|Entfernungen gibt man in cuadras an.
esquina|la esquina|die Ecke|n
vereda|la vereda|der Gehweg|n|Anderswo „acera“.
semaforo|el semáforo|die Ampel|n
plaza|la plaza|der Platz|n
centro|el centro|das Zentrum|n
mapa|el mapa|die Landkarte, der Stadtplan|n|Männlich, obwohl auf -a!
derecha|a la derecha|rechts|ph
izquierda|a la izquierda|links|ph
derecho|derecho|geradeaus|adv|Auch: todo derecho.
cerca|cerca|nah, in der Nähe|adv|cerca de = in der Nähe von
lejos|lejos|weit (weg)|adv
aca|acá|hier|adv|In Argentinien meist „acá“ statt „aquí“.
alla|allá|dort|adv
enfrente|enfrente|gegenüber|adv|enfrente de = gegenüber von
al_lado|al lado de|neben|ph
entre|entre|zwischen|prep
"""),
("Verben & Adjektive", """
ir|ir|gehen, fahren|v|yo voy, vos vas, usted va, nosotros vamos
venir|venir|kommen|v|yo vengo, vos venís
doblar|doblar|abbiegen|v|Doblá a la derecha. = Bieg rechts ab.
seguir|seguir|weitergehen; folgen|v|Seguí derecho. = Geh geradeaus.
bajar|bajar|aussteigen; hinuntergehen|v
subir|subir|einsteigen; hinaufgehen|v
esperar|esperar|warten|v
perderse|perderse|sich verirren|v|Me perdí. = Ich habe mich verirrt.
cruzar|cruzar|überqueren|v
manejar|manejar|Auto fahren|v|Anderswo „conducir“.
alquilar|alquilar|mieten|v
lleno|lleno|voll|adj
libre|libre|frei|adj
directo|directo|direkt|adj
proximo|próximo|nächste(r)|adj
"""),
]
sentences = [
("¿Dónde está la parada del colectivo?","Wo ist die Bushaltestelle?","donde parada colectivo","del_al"),
("¿Qué colectivo va al centro?","Welcher Bus fährt ins Zentrum?","colectivo ir centro","del_al"),
("Necesito cargar la SUBE.","Ich muss die SUBE aufladen.","sube necesitar"),
("¿Dónde compro la SUBE?","Wo kaufe ich die SUBE?","sube donde"),
("Vamos en subte.","Wir fahren mit der U-Bahn.","subte ir","del_al"),
("Voy a tomar un taxi.","Ich nehme ein Taxi.","taxi tomar ir","ir_a"),
("¿Me pedís un remís para las seis?","Bestellst du mir einen Remís für sechs Uhr?","remis seis pedir","voseo"),
("El tren sale a las ocho.","Der Zug fährt um acht.","tren ocho salir"),
("Un pasaje a Córdoba, ida y vuelta.","Eine Fahrkarte nach Córdoba, hin und zurück.","pasaje ida_y_vuelta"),
("¿A qué hora sale el micro?","Wann fährt der Fernbus?","micro a_que_hora salir"),
("¿De qué terminal sale?","Von welchem Busbahnhof fährt er?","terminal que salir"),
("El micro tiene una demora de una hora.","Der Bus hat eine Stunde Verspätung.","micro demora hora"),
("¿Este asiento está libre?","Ist dieser Platz frei?","asiento libre"),
("El colectivo está lleno.","Der Bus ist voll.","colectivo lleno"),
("¿Es un vuelo directo?","Ist es ein Direktflug?","vuelo directo"),
("¿Cómo llego al aeropuerto?","Wie komme ich zum Flughafen?","como aeropuerto llegar","del_al"),
("El avión sale a las once.","Das Flugzeug fliegt um elf.","avion once salir"),
("Mi valija no llegó.","Mein Koffer ist nicht angekommen.","valija no llegar"),
("Llevo solo una mochila.","Ich habe nur einen Rucksack dabei.","mochila"),
("¿Dónde retiro el equipaje?","Wo hole ich das Gepäck ab?","equipaje donde"),
("¿Dónde está la salida?","Wo ist der Ausgang?","salida donde"),
("La llegada es a las tres.","Die Ankunft ist um drei.","llegada tres"),
("¿Cuál es la próxima estación?","Was ist die nächste Station?","estacion proximo cual"),
("¿Dónde tengo que bajar?","Wo muss ich aussteigen?","bajar donde"),
("Hay que subir por adelante.","Man steigt vorne ein.","subir"),
("Esperá acá, por favor.","Warte bitte hier.","esperar aca","imperativo"),
("Es fácil perderse en el centro.","Im Zentrum verirrt man sich leicht.","perderse centro"),
("Seguí derecho dos cuadras.","Geh zwei Blocks geradeaus.","seguir derecho cuadra dos","imperativo"),
("Doblá a la derecha en la esquina.","Bieg an der Ecke rechts ab.","doblar derecha esquina","imperativo"),
("Después doblá a la izquierda.","Danach bieg links ab.","doblar izquierda","imperativo"),
("Cruzá la calle en el semáforo.","Überquer die Strasse an der Ampel.","cruzar calle semaforo","imperativo"),
("Está en la avenida, enfrente de la plaza.","Es ist an der Hauptstrasse, gegenüber vom Platz.","avenida enfrente plaza"),
("El banco está al lado de la farmacia.","Die Bank ist neben der Apotheke.","al_lado"),
("Está entre el hotel y la plaza.","Es ist zwischen dem Hotel und dem Platz.","entre hotel plaza"),
("¿Está cerca o lejos?","Ist es nah oder weit?","cerca lejos"),
("Está muy cerca, a tres cuadras.","Es ist ganz nah, drei Blocks entfernt.","cerca cuadra tres muy"),
("La plaza está a una cuadra.","Der Platz ist einen Block entfernt.","cuadra plaza"),
("Es lejos, mejor tomá un taxi.","Es ist weit, nimm lieber ein Taxi.","lejos taxi tomar","imperativo"),
("Caminá por la vereda.","Geh auf dem Gehweg.","vereda","imperativo"),
("Vamos a pie.","Wir gehen zu Fuss.","a_pie ir","del_al"),
("¿Tenés un mapa?","Hast du einen Stadtplan?","mapa","voseo"),
("Acá está el centro, y allá el puerto.","Hier ist das Zentrum, und dort der Hafen.","aca alla centro"),
("Quiero alquilar un auto.","Ich möchte ein Auto mieten.","alquilar auto"),
("¿Sabés manejar?","Kannst du Auto fahren?","manejar","voseo"),
("Vamos en bicicleta.","Wir fahren mit dem Fahrrad.","bicicleta ir","del_al"),
("El barco a Colonia sale temprano.","Das Schiff nach Colonia fährt früh.","barco temprano salir"),
("¿Podés venir mañana?","Kannst du morgen kommen?","venir","voseo"),
("Mañana voy a ir a Mendoza.","Morgen fahre ich nach Mendoza.","ir","ir_a"),
("¿Cuánto cuesta el boleto?","Wie viel kostet der Fahrschein?","boleto cuanto"),
("Hay que esperar media hora.","Man muss eine halbe Stunde warten.","esperar hora"),
("¿Este colectivo va directo al centro?","Fährt dieser Bus direkt ins Zentrum?","colectivo directo centro","del_al"),
("El próximo tren sale en diez minutos.","Der nächste Zug fährt in zehn Minuten.","proximo tren diez"),
("Voy de la terminal al hotel.","Ich fahre vom Busbahnhof zum Hotel.","terminal hotel ir","del_al"),
]
grammar = [
 {"id":"del_al","title":"al, del, en colectivo, a pie",
  "explanation":"a und de verschmelzen mit el:\n• a + el = al – Voy al centro.\n• de + el = del – la parada del colectivo\nMit la, los, las verschmilzt nichts: a la plaza, de la terminal.\n\nVerkehrsmittel mit en: en colectivo, en subte, en tren, en auto.\nAusnahme: a pie (zu Fuss).\n\nRichtung: a = nach/zu (Voy a Córdoba), de = von/aus.",
  "examples":[{"text":"Voy al aeropuerto en taxi.","de":"Ich fahre mit dem Taxi zum Flughafen."},{"text":"Salgo del hotel a las diez.","de":"Ich verlasse das Hotel um zehn."}]},
 {"id":"ir_a","title":"ir a + Infinitiv – die nahe Zukunft",
  "explanation":"ir (gehen) ist unregelmässig:\n• yo voy, vos vas, usted/él va, nosotros vamos, ustedes/ellos van\n\nMit a + Infinitiv drückt man aus, was man vorhat – im Alltag die übliche Zukunft:\n• Voy a tomar un taxi. – Ich werde ein Taxi nehmen.\n• Vamos a comer. – Wir gehen essen / Lass uns essen.",
  "examples":[{"text":"¿Qué vas a hacer mañana?","de":"Was machst du morgen?"},{"text":"Vamos a ir en micro.","de":"Wir fahren mit dem Bus."}]},
 {"id":"imperativo","title":"Wegbeschreibung: der Imperativ mit vos",
  "explanation":"Der vos-Imperativ ist leicht: Infinitiv ohne -r, Betonung auf der letzten Silbe:\n• doblar → doblá (bieg ab)\n• tomar → tomá (nimm)\n• esperar → esperá (warte)\n• cruzar → cruzá (überquere)\n• seguir → seguí (geh weiter)\n• venir → vení (komm)\nAusnahme: ir → andá (geh!).\n\nZu usted: doble, tome, espere, cruce, siga, venga.",
  "examples":[{"text":"Seguí derecho y doblá a la izquierda.","de":"Geh geradeaus und bieg links ab."},{"text":"Siga dos cuadras, por favor.","de":"Gehen Sie bitte zwei Blocks weiter."}]},
]
wg = {"ir":["ir_a"],"doblar":["imperativo"],"seguir":["imperativo"],"cruzar":["imperativo"],"esperar":["imperativo"]}
make('05_en_camino.json','es-AR-05',5,'Unterwegs','En camino',groups,sentences,grammar,wg)
