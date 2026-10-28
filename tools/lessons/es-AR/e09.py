from _common import make
groups = [
("Wetter & Jahreszeiten", """
clima|el clima|das Klima; das Wetter|n|Männlich, obwohl auf -a!
tiempo|el tiempo|das Wetter; die Zeit|n|¿Qué tiempo hace? = Wie ist das Wetter?
sol|el sol|die Sonne|n|tomar sol = sich sonnen
lluvia|la lluvia|der Regen|n
llover|llover|regnen|v|llueve = es regnet
viento|el viento|der Wind|n
nublado|nublado|bewölkt|adj
calor|el calor|die Hitze|n|hace calor = es ist heiss
humedad|la humedad|die Luftfeuchtigkeit|n|In Buenos Aires im Sommer sehr hoch.
tormenta|la tormenta|das Gewitter, der Sturm|n
grado|el grado|das Grad|n
pronostico|el pronóstico|die Wettervorhersage|n
verano|el verano|der Sommer|n|In Argentinien von Dezember bis März!
invierno|el invierno|der Winter|n|Juni bis September.
"""),
("Strand & Natur", """
playa|la playa|der Strand|n
mar|el mar|das Meer|n
rio|el río|der Fluss|n|Der Río de la Plata ist so breit, dass er wie ein Meer aussieht.
arena|la arena|der Sand|n
ola|la ola|die Welle|n
malla|la malla|die Badehose, der Badeanzug|n|Argentinisch; anderswo „traje de baño“.
sombrilla|la sombrilla|der Sonnenschirm|n
reposera|la reposera|der Liegestuhl|n|Argentinisch.
nadar|nadar|schwimmen|v
montana|la montaña|der Berg; das Gebirge|n
lago|el lago|der See|n
sombra|la sombra|der Schatten|n
"""),
("Freizeit", """
futbol|el fútbol|der Fussball|n
partido|el partido|das Spiel (Sport)|n
cancha|la cancha|der Sportplatz; das Stadion|n
tango|el tango|der Tango|n
musica|la música|die Musik|n
museo|el museo|das Museum|n
teatro|el teatro|das Theater|n
cine|el cine|das Kino|n
excursion|la excursión|der Ausflug|n
entrada|la entrada|die Eintrittskarte; der Eingang|n
fin_de_semana|el fin de semana|das Wochenende|n
sacar_fotos|sacar fotos|Fotos machen|ph
tranquilo|tranquilo|ruhig; entspannt|adj|¡Tranquilo! = Keine Sorge!
afuera|afuera|draussen|adv
adentro|adentro|drinnen|adv
"""),
("Verben", """
hacer|hacer|machen, tun|v|yo hago, vos hacés; hace calor = es ist heiss
caminar|caminar|zu Fuss gehen, spazieren|v
bailar|bailar|tanzen|v
jugar|jugar|spielen|v|yo juego, vos jugás; jugar al fútbol
descansar|descansar|sich ausruhen|v
ver|ver|sehen, schauen|v|yo veo, vos ves
escuchar|escuchar|hören, zuhören|v
leer|leer|lesen|v
"""),
]
sentences = [
("¿Qué tiempo hace hoy?","Wie ist das Wetter heute?","tiempo hacer","clima"),
("Hace mucho calor.","Es ist sehr heiss.","calor hacer mucho","clima"),
("Hace frío a la noche.","Nachts ist es kalt.","frio hacer noche","clima"),
("Hay mucho sol.","Es ist sehr sonnig.","sol hay mucho","clima"),
("Está nublado.","Es ist bewölkt.","nublado","clima"),
("Va a llover a la tarde.","Am Nachmittag wird es regnen.","llover tarde_n","ir_a"),
("Llueve mucho en invierno.","Im Winter regnet es viel.","llover invierno mucho","clima"),
("Con lluvia no vamos a la playa.","Bei Regen gehen wir nicht an den Strand.","lluvia playa"),
("Hay mucho viento en la costa.","An der Küste ist viel Wind.","viento hay mucho","clima"),
("Hay mucha humedad en Buenos Aires.","In Buenos Aires ist die Luftfeuchtigkeit hoch.","humedad mucho"),
("Se viene una tormenta.","Ein Gewitter zieht auf.","tormenta"),
("Hace treinta grados.","Es sind dreissig Grad.","grado treinta hacer","clima"),
("¿Viste el pronóstico?","Hast du die Wettervorhersage gesehen?","pronostico ver"),
("El clima en Mendoza es seco.","Das Klima in Mendoza ist trocken.","clima"),
("En diciembre es verano en Argentina.","Im Dezember ist in Argentinien Sommer.","verano argentina"),
("En invierno hace frío en el sur.","Im Winter ist es im Süden kalt.","invierno frio hacer","clima"),
("Vamos a la playa.","Gehen wir an den Strand.","playa ir","del_al"),
("El mar está frío hoy.","Das Meer ist heute kalt.","mar frio"),
("¡Cuidado con la ola!","Vorsicht vor der Welle!","ola cuidado"),
("La arena está muy caliente.","Der Sand ist sehr heiss.","arena caliente muy"),
("Me olvidé la malla.","Ich habe die Badehose vergessen.","malla"),
("¿Cuánto cuesta alquilar una sombrilla?","Wie viel kostet es, einen Sonnenschirm zu mieten?","sombrilla alquilar cuanto"),
("Estoy tomando sol en la reposera.","Ich sonne mich im Liegestuhl.","reposera sol","estar_gerundio"),
("¿Se puede nadar acá?","Kann man hier schwimmen?","nadar aca"),
("Prefiero la sombra.","Ich bin lieber im Schatten.","sombra"),
("Hacemos una excursión a la montaña.","Wir machen einen Ausflug in die Berge.","excursion montana hacer","presente"),
("El lago es hermoso.","Der See ist wunderschön.","lago"),
("Caminamos por el río.","Wir spazieren am Fluss entlang.","rio caminar","presente"),
("¿Te gusta el fútbol?","Magst du Fussball?","futbol gustar","gustar"),
("El domingo hay partido de Boca.","Am Sonntag spielt Boca.","partido hay"),
("¿Dónde queda la cancha?","Wo ist das Stadion?","cancha donde"),
("Quiero ver un show de tango.","Ich möchte eine Tangoshow sehen.","tango ver querer"),
("¿Bailás tango?","Tanzt du Tango?","bailar tango","voseo"),
("Me gusta escuchar música.","Ich höre gern Musik.","escuchar musica gustar","gustar"),
("¿El museo está abierto los lunes?","Ist das Museum montags geöffnet?","museo abierto"),
("Vamos al teatro esta noche.","Wir gehen heute Abend ins Theater.","teatro noche","del_al"),
("¿Vamos al cine?","Gehen wir ins Kino?","cine","del_al"),
("Dos entradas, por favor.","Zwei Eintrittskarten, bitte.","entrada dos"),
("¿Puedo sacar fotos?","Darf ich Fotos machen?","sacar_fotos"),
("Los chicos juegan en la plaza.","Die Kinder spielen auf dem Platz.","jugar chicos plaza","presente"),
("¿Jugás al fútbol?","Spielst du Fussball?","jugar futbol","voseo"),
("Hoy quiero descansar.","Heute möchte ich mich ausruhen.","descansar querer"),
("Leo un libro en la playa.","Ich lese ein Buch am Strand.","leer playa","presente"),
("¿Qué hacés el fin de semana?","Was machst du am Wochenende?","hacer fin_de_semana","voseo"),
("El fin de semana vamos al río.","Am Wochenende fahren wir an den Fluss.","fin_de_semana rio"),
("Estamos mirando el partido.","Wir schauen gerade das Spiel.","mirar partido","estar_gerundio"),
("Está todo muy tranquilo.","Es ist alles sehr ruhig.","tranquilo muy"),
("Comemos afuera.","Wir essen draussen.","afuera comer","presente"),
("Mejor nos quedamos adentro.","Wir bleiben lieber drinnen.","adentro mejor"),
("¿Qué estás haciendo?","Was machst du gerade?","hacer que","estar_gerundio"),
("Hablamos, comemos y bailamos toda la noche.","Wir reden, essen und tanzen die ganze Nacht.","bailar noche","presente"),
("Vivo cerca del mar.","Ich wohne in der Nähe des Meeres.","mar cerca vivir","presente"),
("Ella baila muy bien.","Sie tanzt sehr gut.","bailar bien muy","presente"),
]
grammar = [
 {"id":"clima","title":"Über das Wetter reden",
  "explanation":"• hace + Nomen: hace calor (heiss), hace frío (kalt), hace sol, hace viento, hace 30 grados\n• hay + Nomen: hay sol, hay viento, hay humedad\n• estar + Adjektiv: está nublado, está lindo (schönes Wetter), está feo\n• llueve – es regnet; va a llover – es wird regnen\n\nAchtung Südhalbkugel: Im Dezember ist Sommer, im Juli Winter.",
  "examples":[{"text":"Hoy hace mucho calor.","de":"Heute ist es sehr heiss."},{"text":"Está lindo para ir a la playa.","de":"Schönes Wetter für den Strand."}]},
 {"id":"presente","title":"Präsens der regelmässigen Verben",
  "explanation":"Drei Gruppen: -ar, -er, -ir.\n\nhablar: hablo, hablás, habla, hablamos, hablan\ncomer: como, comés, come, comemos, comen\nvivir: vivo, vivís, vive, vivimos, viven\n\nReihenfolge: yo, vos, usted/él/ella, nosotros, ustedes/ellos.\nDie vos-Form ist immer auf der Endung betont: caminás, leés, escribís.\n\nViele Verben ändern den Stamm (aber nicht bei vos!): jugar → juego, aber vos jugás; querer → quiero, aber vos querés.",
  "examples":[{"text":"¿Dónde vivís? – Vivo en Córdoba.","de":"Wo wohnst du? – Ich wohne in Córdoba."},{"text":"Los sábados comemos asado.","de":"Samstags essen wir Asado."}]},
 {"id":"estar_gerundio","title":"estar + -ando / -iendo – gerade tun",
  "explanation":"Was gerade jetzt passiert, drückt man mit estar + Gerundium aus:\n• -ar → -ando: hablar → hablando, mirar → mirando\n• -er/-ir → -iendo: comer → comiendo, escribir → escribiendo\n• Besonders: leer → leyendo, llover → lloviendo\n\n• Estoy comiendo. – Ich esse gerade.\n• Está lloviendo. – Es regnet gerade.",
  "examples":[{"text":"¿Qué estás haciendo? – Estoy leyendo.","de":"Was machst du gerade? – Ich lese."},{"text":"Estamos tomando mate.","de":"Wir trinken gerade Mate."}]},
]
wg = {"llover":["clima"],"calor":["clima"],"hacer":["clima"],"caminar":["presente"],"bailar":["presente"],"leer":["presente"]}
make('09_clima_playa_tiempo_libre.json','es-AR-09',9,'Wetter, Strand & Freizeit','Clima, playa y tiempo libre',groups,sentences,grammar,wg)
