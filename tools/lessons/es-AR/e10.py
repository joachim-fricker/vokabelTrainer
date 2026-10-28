from _common import make
groups = [
("Wochentage", """
lunes|el lunes|der Montag|n
martes|el martes|der Dienstag|n
miercoles|el miércoles|der Mittwoch|n
jueves|el jueves|der Donnerstag|n
viernes|el viernes|der Freitag|n
sabado|el sábado|der Samstag|n
domingo|el domingo|der Sonntag|n
semana|la semana|die Woche|n|la semana que viene = nächste Woche
dia|el día|der Tag|n|Männlich, obwohl auf -a!
"""),
("Monate", """
enero|enero|Januar|n
febrero|febrero|Februar|n
marzo|marzo|März|n
abril|abril|April|n
mayo|mayo|Mai|n
junio|junio|Juni|n
julio|julio|Juli|n
agosto|agosto|August|n
septiembre|septiembre|September|n
octubre|octubre|Oktober|n
noviembre|noviembre|November|n
diciembre|diciembre|Dezember|n
mes|el mes|der Monat|n
"""),
("Zeitangaben", """
hoy|hoy|heute|adv
manana_adv|mañana|morgen|adv|Mit Artikel: la mañana = der Morgen.
ayer|ayer|gestern|adv
pasado_manana|pasado mañana|übermorgen|ph
siempre|siempre|immer|adv
nunca|nunca|nie|adv
a_veces|a veces|manchmal|ph
todos_los_dias|todos los días|jeden Tag|ph
cada|cada|jede(r, s)|adj|Unveränderlich: cada día, cada semana.
despues|después|danach, später|adv
antes|antes|vorher|adv|antes de = vor
ya|ya|schon; jetzt gleich|adv
todavia|todavía|noch|adv|todavía no = noch nicht
fecha|la fecha|das Datum|n
cumpleanos|el cumpleaños|der Geburtstag|n
feriado|el feriado|der Feiertag|n|Argentinien hat viele Feiertage, oft auf Montag verschoben.
"""),
("Alltag", """
levantarse|levantarse|aufstehen|v|me levanto, vos te levantás
acostarse|acostarse|ins Bett gehen|v|me acuesto, vos te acostás
dormir|dormir|schlafen|v|duermo, vos dormís
ducharse|ducharse|duschen|v
desayunar|desayunar|frühstücken|v
cocinar|cocinar|kochen|v
limpiar|limpiar|putzen|v
lavar|lavar|waschen|v
casa|la casa|das Haus|n|en casa = zu Hause
siesta|la siesta|der Mittagsschlaf|n|dormir la siesta = Mittagsschlaf halten
mensaje|el mensaje|die Nachricht|n
"""),
("Wichtige Verben", """
poder|poder|können, dürfen|v|puedo, vos podés, puede
saber|saber|wissen; können (gelernt)|v|sé, vos sabés, sabe
decir|decir|sagen|v|digo, vos decís, dice
empezar|empezar|anfangen, beginnen|v|empiezo, vos empezás
terminar|terminar|beenden, fertig sein|v
volver|volver|zurückkommen|v|vuelvo, vos volvés
escribir|escribir|schreiben|v
mandar|mandar|schicken|v|Mandame un mensaje. = Schick mir eine Nachricht.
"""),
]
sentences = [
("Hoy es lunes.","Heute ist Montag.","hoy lunes","fechas"),
("Mañana es martes.","Morgen ist Dienstag.","manana_adv martes","fechas"),
("Ayer fue domingo.","Gestern war Sonntag.","ayer domingo","pasado"),
("El miércoles vamos al museo.","Am Mittwoch gehen wir ins Museum.","miercoles museo","fechas"),
("Los jueves hay feria.","Donnerstags ist Markt.","jueves feria hay","fechas"),
("El viernes a la noche salimos.","Freitagabend gehen wir aus.","viernes noche salir","fechas"),
("El sábado hacemos un asado.","Am Samstag machen wir Asado.","sabado asado hacer","fechas"),
("Me quedo una semana.","Ich bleibe eine Woche.","semana quedarse"),
("¿Qué día es hoy?","Welcher Tag ist heute?","dia hoy que","fechas"),
("Llegamos el dos de enero.","Wir kommen am zweiten Januar an.","enero dos llegar","fechas"),
("En febrero hace mucho calor.","Im Februar ist es sehr heiss.","febrero calor","clima"),
("Marzo es el mes de la vendimia en Mendoza.","Der März ist in Mendoza der Monat der Weinlese.","marzo mes"),
("En abril empieza el otoño.","Im April beginnt der Herbst.","abril empezar"),
("El veinticinco de mayo es feriado.","Der 25. Mai ist ein Feiertag.","mayo feriado","fechas"),
("En junio y julio es invierno.","Im Juni und Juli ist Winter.","junio julio invierno"),
("En agosto hace frío.","Im August ist es kalt.","agosto frio","clima"),
("Mi cumpleaños es en septiembre.","Mein Geburtstag ist im September.","cumpleanos septiembre mi"),
("En octubre vuelvo a Alemania.","Im Oktober fahre ich nach Deutschland zurück.","octubre volver alemania"),
("Viajo en noviembre.","Ich reise im November.","noviembre"),
("En diciembre empieza el verano.","Im Dezember beginnt der Sommer.","diciembre verano empezar"),
("¿Qué fecha es hoy?","Welches Datum ist heute?","fecha hoy","fechas"),
("Nos vemos pasado mañana.","Wir sehen uns übermorgen.","pasado_manana nos_vemos"),
("Siempre tomo mate a la mañana.","Ich trinke morgens immer Mate.","siempre mate tomar"),
("Nunca como carne.","Ich esse nie Fleisch.","nunca carne comer"),
("A veces duermo la siesta.","Manchmal mache ich Mittagsschlaf.","a_veces dormir siesta","verbos_irregulares"),
("Trabajo todos los días.","Ich arbeite jeden Tag.","todos_los_dias trabajar"),
("Primero desayuno y después me ducho.","Zuerst frühstücke ich und danach dusche ich.","desayunar despues ducharse"),
("Antes de salir, cierro la puerta.","Bevor ich gehe, schliesse ich die Tür.","antes salir puerta"),
("Ya terminé.","Ich bin schon fertig.","ya terminar","pasado"),
("Todavía no sé.","Ich weiss es noch nicht.","todavia saber no","verbos_irregulares"),
("Me levanto a las siete.","Ich stehe um sieben auf.","levantarse siete"),
("¿A qué hora te levantás?","Um wie viel Uhr stehst du auf?","levantarse a_que_hora","voseo"),
("Me acuesto tarde.","Ich gehe spät ins Bett.","acostarse tarde"),
("Dormí muy bien.","Ich habe sehr gut geschlafen.","dormir bien muy","pasado"),
("¿Dormiste bien?","Hast du gut geschlafen?","dormir bien","pasado"),
("Hoy cocino yo.","Heute koche ich.","cocinar hoy yo"),
("Tengo que limpiar la casa.","Ich muss das Haus putzen.","limpiar casa tener_que","tener_que"),
("¿Dónde puedo lavar la ropa?","Wo kann ich Wäsche waschen?","lavar ropa poder","verbos_irregulares"),
("No voy a poder ir.","Ich werde nicht kommen können.","poder ir","ir_a"),
("¿Sabés dónde queda?","Weisst du, wo das ist?","saber donde","voseo"),
("¿Qué dijiste?","Was hast du gesagt?","decir que","pasado"),
("¿Cómo se dice en español?","Wie sagt man das auf Spanisch?","decir como espanol"),
("La clase empieza a las nueve.","Der Unterricht beginnt um neun.","empezar nueve"),
("¿A qué hora termina?","Um wie viel Uhr endet es?","terminar a_que_hora"),
("Quiero volver a Argentina.","Ich möchte nach Argentinien zurückkommen.","volver argentina querer"),
("Te escribo mañana.","Ich schreibe dir morgen.","escribir manana_adv"),
("Mandame un mensaje.","Schick mir eine Nachricht.","mandar mensaje","imperativo"),
("Estoy en casa.","Ich bin zu Hause.","casa estar","ser_estar"),
("Ayer fuimos a la playa.","Gestern waren wir am Strand.","ayer playa ir","pasado"),
("Cada día aprendo algo nuevo.","Jeden Tag lerne ich etwas Neues.","cada dia nuevo"),
("Ya es tarde, me voy.","Es ist schon spät, ich gehe.","ya tarde ir"),
("Todavía tengo tiempo.","Ich habe noch Zeit.","todavia tiempo tener"),
("La semana que viene tengo vacaciones.","Nächste Woche habe ich Urlaub.","semana vacaciones"),
("¿Qué hiciste el fin de semana?","Was hast du am Wochenende gemacht?","hacer fin_de_semana","pasado"),
("El martes es mi cumpleaños.","Am Dienstag ist mein Geburtstag.","martes cumpleanos mi"),
("El domingo es feriado y está todo cerrado.","Sonntag ist Feiertag und alles ist zu.","domingo feriado cerrado"),
]
grammar = [
 {"id":"fechas","title":"Wochentage, Monate, Datum",
  "explanation":"Wochentage und Monate schreibt man klein.\n\n• el lunes – am Montag\n• los lunes – montags (jeden Montag)\n• el fin de semana – am Wochenende\n\nDatum: el + Zahl + de + Monat\n• el 2 de enero – der 2. Januar (Grundzahl, nur der 1. auch: el primero)\n• ¿Qué día es hoy? – Hoy es martes.\n• ¿Qué fecha es hoy? – Hoy es 15 de diciembre.\n\nMonate ohne Artikel: en marzo – im März.",
  "examples":[{"text":"Llego el viernes 5 de diciembre.","de":"Ich komme am Freitag, dem 5. Dezember, an."},{"text":"Los domingos está todo cerrado.","de":"Sonntags ist alles zu."}]},
 {"id":"verbos_irregulares","title":"Wichtige unregelmässige Verben",
  "explanation":"yo – vos – usted/él – nosotros – ustedes\n• poder: puedo, podés, puede, podemos, pueden\n• querer: quiero, querés, quiere, queremos, quieren\n• saber: sé, sabés, sabe, sabemos, saben\n• decir: digo, decís, dice, decimos, dicen\n• hacer: hago, hacés, hace, hacemos, hacen\n• dormir: duermo, dormís, duerme, dormimos, duermen\n• ir: voy, vas, va, vamos, van\n\nPraktisch beim vos: Der Stamm bleibt fast immer regelmässig (podés, querés, dormís) – nur yo, usted und ustedes ändern sich.\n\nsaber + Infinitiv = etwas können (gelernt): ¿Sabés nadar?",
  "examples":[{"text":"¿Podés venir? – No puedo.","de":"Kannst du kommen? – Ich kann nicht."},{"text":"No sé qué decir.","de":"Ich weiss nicht, was ich sagen soll."}]},
 {"id":"pasado","title":"Vergangenheit: Ayer fui …",
  "explanation":"In Argentinien erzählt man Vergangenes fast immer mit dem einfachen Perfekt (pretérito), auch für „heute“ und „gerade eben“:\n\n-ar (terminar): terminé, terminaste, terminó, terminamos, terminaron\n-er/-ir (comer): comí, comiste, comió, comimos, comieron\n\nHäufige Ausnahmen:\n• ir und ser: fui, fuiste, fue, fuimos, fueron\n• hacer: hice, hiciste, hizo, hicimos, hicieron\n• decir: dije, dijiste, dijo, dijimos, dijeron\n• tener: tuve, tuviste, tuvo …\n\n„Ya terminé.“ – Ich bin schon fertig.",
  "examples":[{"text":"Ayer comimos un asado.","de":"Gestern haben wir Asado gegessen."},{"text":"¿Qué hiciste hoy?","de":"Was hast du heute gemacht?"}]},
]
wg = {"lunes":["fechas"],"fecha":["fechas"],"enero":["fechas"],"poder":["verbos_irregulares"],"saber":["verbos_irregulares"],"decir":["verbos_irregulares"],"dormir":["verbos_irregulares"],"ayer":["pasado"]}
make('10_vida_cotidiana.json','es-AR-10',10,'Alltag & Zeit','Vida cotidiana y tiempo',groups,sentences,grammar,wg)
