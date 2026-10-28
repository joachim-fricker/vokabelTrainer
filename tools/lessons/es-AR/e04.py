from _common import make
groups = [
("Ankommen & Einchecken", """
hotel|el hotel|das Hotel|n
hostel|el hostel|das Hostel|n
hospedaje|el hospedaje|die Unterkunft|n
departamento|el departamento|die (Ferien-)Wohnung|n|Kurz: el depto.
recepcion|la recepción|die Rezeption|n
check_in|el check-in|der Check-in|n
check_out|el check-out|der Check-out|n
pasaporte|el pasaporte|der Reisepass|n
documento|el documento|der Ausweis|n|Der argentinische Personalausweis heisst DNI.
huesped|el huésped|der Gast (im Hotel)|n
llave|la llave|der Schlüssel|n
confirmar|confirmar|bestätigen|v
reservar|reservar|reservieren|v
"""),
("Das Zimmer", """
habitacion|la habitación|das Zimmer|n|Auch: el cuarto.
habitacion_simple|la habitación simple|das Einzelzimmer|n
habitacion_doble|la habitación doble|das Doppelzimmer|n|Mit Doppelbett: cama matrimonial.
cama|la cama|das Bett|n
almohada|la almohada|das Kopfkissen|n
frazada|la frazada|die Wolldecke|n|Argentinisch; anderswo „manta“.
toalla|la toalla|das Handtuch|n
ducha|la ducha|die Dusche|n
bano|el baño|das Bad; die Toilette|n
aire_acondicionado|el aire acondicionado|die Klimaanlage|n|Kurz: el aire.
ventilador|el ventilador|der Ventilator|n
calefaccion|la calefacción|die Heizung|n
ventana|la ventana|das Fenster|n
puerta|la puerta|die Tür|n
balcon|el balcón|der Balkon|n
vista|la vista|die Aussicht|n|vista al mar = Meerblick
piso|el piso|das Stockwerk|n|planta baja = Erdgeschoss
ascensor|el ascensor|der Aufzug|n
escalera|la escalera|die Treppe|n
"""),
("Service & Ausstattung", """
wifi|el wifi|das WLAN|n|Ausgesprochen „uifi“.
contrasena|la contraseña|das Passwort|n|Auch: la clave.
pileta|la pileta|das Schwimmbecken, der Pool|n|Argentinisch; anderswo „piscina“.
estacionamiento|el estacionamiento|der Parkplatz|n
caja_fuerte|la caja fuerte|der Safe|n
limpieza|la limpieza|die Reinigung, das Putzen|n
lavadero|el lavadero|die Wäscherei|n|Auch: la lavandería.
enchufe|el enchufe|die Steckdose; der Stecker|n|In Argentinien Typ I (schräge Stifte) – Adapter nötig.
cargador|el cargador|das Ladegerät|n
incluido|incluido|inbegriffen|adj
"""),
("Probleme", """
problema|el problema|das Problem|n|Männlich, obwohl auf -a!
roto|roto|kaputt|adj
sucio|sucio|schmutzig|adj
limpio|limpio|sauber|adj
ruidoso|ruidoso|laut (lärmig)|adj
funcionar|funcionar|funktionieren|v
agua_caliente|el agua caliente|das warme Wasser|n:f
"""),
("Fragewörter & Verben", """
donde|¿dónde?|wo?|adv
cuando|¿cuándo?|wann?|adv
cual|¿cuál?|welche(r)?; was? (Auswahl)|pron
que|¿qué?|was?|pron
quien|¿quién?|wer?|pron
por_que|¿por qué?|warum?|adv|Antwort: porque (weil) – zusammen und ohne Akzent.
hay|hay|es gibt|v
tener|tener|haben|v|yo tengo, vos tenés, usted tiene
necesitar|necesitar|brauchen|v
quedarse|quedarse|bleiben (übernachten)|v|Me quedo dos noches.
llegar|llegar|ankommen|v
salir|salir|hinausgehen; abreisen, abfahren|v|yo salgo, vos salís
"""),
]
sentences = [
("Tengo una reserva a nombre de Fricker.","Ich habe eine Reservierung auf den Namen Fricker.","tener reserva","tener_hay"),
("¿A qué hora es el check-in?","Um wie viel Uhr ist der Check-in?","check_in a_que_hora","preguntas"),
("El check-out es a las diez.","Der Check-out ist um zehn.","check_out diez"),
("¿Me muestra su pasaporte, por favor?","Zeigen Sie mir bitte Ihren Pass?","pasaporte por_favor","vos_usted"),
("Necesito el documento de todos los huéspedes.","Ich brauche den Ausweis aller Gäste.","necesitar documento huesped","necesitar"),
("¿Tienen una habitación doble?","Habt ihr ein Doppelzimmer?","habitacion_doble tener","tener_hay"),
("Quiero una habitación simple.","Ich möchte ein Einzelzimmer.","habitacion_simple querer"),
("Me quedo tres noches.","Ich bleibe drei Nächte.","quedarse tres"),
("¿Cuánto cuesta la noche?","Wie viel kostet die Nacht?","cuanto noche"),
("¿El desayuno está incluido?","Ist das Frühstück inbegriffen?","desayuno incluido"),
("Acá tiene la llave.","Hier haben Sie den Schlüssel.","llave"),
("La habitación está en el tercer piso.","Das Zimmer ist im dritten Stock.","habitacion piso"),
("¿Dónde está el ascensor?","Wo ist der Aufzug?","donde ascensor","preguntas"),
("La escalera está a la derecha.","Die Treppe ist rechts.","escalera"),
("¿Cuál es la contraseña del wifi?","Wie lautet das WLAN-Passwort?","cual contrasena wifi","preguntas"),
("¿Hay wifi en la habitación?","Gibt es WLAN im Zimmer?","hay wifi habitacion","tener_hay"),
("¿El hotel tiene pileta?","Hat das Hotel einen Pool?","hotel pileta tener","tener_hay"),
("¿Hay estacionamiento?","Gibt es einen Parkplatz?","hay estacionamiento","tener_hay"),
("¿La habitación tiene caja fuerte?","Hat das Zimmer einen Safe?","caja_fuerte habitacion","tener_hay"),
("¿A qué hora es la limpieza?","Wann wird geputzt?","limpieza a_que_hora"),
("¿Hay un lavadero cerca?","Gibt es eine Wäscherei in der Nähe?","lavadero hay","tener_hay"),
("¿Dónde hay un enchufe?","Wo gibt es eine Steckdose?","donde enchufe hay","preguntas"),
("Me olvidé el cargador.","Ich habe das Ladegerät vergessen.","cargador"),
("La ducha no funciona.","Die Dusche funktioniert nicht.","ducha funcionar no"),
("No hay agua caliente.","Es gibt kein warmes Wasser.","agua_caliente hay no","tener_hay"),
("El aire acondicionado está roto.","Die Klimaanlage ist kaputt.","aire_acondicionado roto"),
("¿Funciona la calefacción?","Funktioniert die Heizung?","calefaccion funcionar"),
("El ventilador hace mucho ruido.","Der Ventilator macht viel Lärm.","ventilador"),
("Es un barrio muy ruidoso.","Es ist ein sehr lautes Viertel.","ruidoso muy"),
("¿Me trae otra toalla, por favor?","Bringen Sie mir bitte noch ein Handtuch?","toalla traer por_favor"),
("El baño está sucio.","Das Bad ist schmutzig.","bano sucio"),
("El baño ya está limpio.","Das Bad ist jetzt sauber.","bano limpio"),
("Necesito otra almohada.","Ich brauche noch ein Kopfkissen.","almohada necesitar","necesitar"),
("¿Me trae una frazada más?","Bringen Sie mir noch eine Decke?","frazada traer"),
("La cama es muy cómoda.","Das Bett ist sehr bequem.","cama muy"),
("¿Puedo cambiar de habitación?","Kann ich das Zimmer wechseln?","cambiar habitacion"),
("Tenemos un problema con la ventana.","Wir haben ein Problem mit dem Fenster.","problema ventana con tener","tener_hay"),
("La puerta no cierra.","Die Tür schliesst nicht.","puerta no"),
("¿La habitación tiene balcón?","Hat das Zimmer einen Balkon?","balcon habitacion"),
("¡Qué linda vista!","Was für eine schöne Aussicht!","vista"),
("¿Cuándo llegan ustedes?","Wann kommt ihr an?","cuando ustedes llegar","preguntas"),
("Llegamos tarde a la noche.","Wir kommen spät abends an.","llegar tarde noche"),
("Mañana salgo temprano.","Morgen reise ich früh ab.","salir temprano"),
("¿Quién habla inglés acá?","Wer spricht hier Englisch?","quien hablar","preguntas"),
("¿Por qué no funciona la llave?","Warum funktioniert der Schlüssel nicht?","por_que llave funcionar","preguntas"),
("¿Qué hay cerca del hotel?","Was gibt es in der Nähe des Hotels?","que hay hotel","preguntas"),
("Quiero confirmar la reserva.","Ich möchte die Reservierung bestätigen.","confirmar reserva"),
("Quiero reservar un departamento.","Ich möchte eine Ferienwohnung reservieren.","reservar departamento"),
("El hostel es barato.","Das Hostel ist günstig.","hostel barato"),
("Es un hospedaje familiar.","Es ist eine familiäre Unterkunft.","hospedaje"),
("La recepción está abierta las 24 horas.","Die Rezeption ist rund um die Uhr geöffnet.","recepcion"),
("Necesito un adaptador para el enchufe.","Ich brauche einen Adapter für die Steckdose.","enchufe necesitar","necesitar"),
("El huésped de la habitación diez ya salió.","Der Gast aus Zimmer zehn ist schon abgereist.","huesped habitacion diez salir"),
]
grammar = [
 {"id":"preguntas","title":"Fragewörter",
  "explanation":"Fragewörter tragen immer einen Akzent:\n• ¿dónde? – wo?  (¿Dónde está …? – Wo ist …?)\n• ¿cuándo? – wann?\n• ¿cuánto/cuánta? – wie viel?  ¿cuántos/cuántas? – wie viele?\n• ¿cuál? / ¿cuáles? – welche(r)? – auch „was“ bei Auswahl: ¿Cuál es la contraseña?\n• ¿qué? – was?\n• ¿quién? – wer?\n• ¿cómo? – wie?\n• ¿por qué? – warum?  Antwort: porque (weil).\n\nFragen beginnen mit ¿ und Ausrufe mit ¡.",
  "examples":[{"text":"¿Dónde está la recepción?","de":"Wo ist die Rezeption?"},{"text":"¿Cuántas noches?","de":"Wie viele Nächte?"},{"text":"¿Por qué no? – Porque está roto.","de":"Warum nicht? – Weil es kaputt ist."}]},
 {"id":"tener_hay","title":"tener – haben, und hay – es gibt",
  "explanation":"tener (haben) im Präsens:\n• yo tengo – ich habe\n• vos tenés – du hast\n• usted / él / ella tiene – Sie haben, er/sie hat\n• nosotros tenemos – wir haben\n• ustedes / ellos tienen – ihr habt, sie haben\n\n„Es gibt“ heisst hay – unveränderlich, auch im Plural:\n¿Hay wifi? – Gibt es WLAN?  Hay dos camas. – Es gibt zwei Betten.\n\nWo etwas Bestimmtes ist, fragt man mit estar: ¿Dónde está el baño?",
  "examples":[{"text":"Tengo una reserva.","de":"Ich habe eine Reservierung."},{"text":"¿Tienen toallas?","de":"Haben Sie Handtücher?"},{"text":"No hay agua caliente.","de":"Es gibt kein warmes Wasser."}]},
 {"id":"necesitar","title":"necesitar – brauchen",
  "explanation":"Anders als im Portugiesischen (precisar de) steht nach necesitar kein de:\n• Necesito una toalla. – Ich brauche ein Handtuch.\n• Necesito el pasaporte. – Ich brauche den Pass.\n\nMit Verb heisst es „müssen“:\n• Necesito salir temprano. – Ich muss früh los.\n\nyo necesito, vos necesitás, usted necesita.",
  "examples":[{"text":"Necesito ayuda.","de":"Ich brauche Hilfe."},{"text":"¿Necesitás algo?","de":"Brauchst du etwas?"}]},
]
wg = {"donde":["preguntas"],"cuando":["preguntas"],"cual":["preguntas"],"que":["preguntas"],"quien":["preguntas"],"por_que":["preguntas"],"tener":["tener_hay"],"hay":["tener_hay"],"necesitar":["necesitar"]}
make('04_hotel.json','es-AR-04',4,'Hotel & Unterkunft','Hotel y alojamiento',groups,sentences,grammar,wg)
