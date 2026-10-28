from _common import make
groups = [
("Körper", """
cabeza|la cabeza|der Kopf|n
panza|la panza|der Bauch|n|Umgangssprachlich; formell: el estómago.
garganta|la garganta|der Hals (innen)|n
espalda|la espalda|der Rücken|n
pie|el pie|der Fuss|n
pierna|la pierna|das Bein|n
brazo|el brazo|der Arm|n
mano|la mano|die Hand|n|Weiblich, obwohl auf -o!
ojo|el ojo|das Auge|n
oido|el oído|das Ohr (innen)|n
diente|el diente|der Zahn|n
muela|la muela|der Backenzahn|n|dolor de muela = Zahnschmerzen
pecho|el pecho|die Brust|n
rodilla|la rodilla|das Knie|n
"""),
("Beschwerden", """
dolor|el dolor|der Schmerz|n|dolor de cabeza = Kopfschmerzen
fiebre|la fiebre|das Fieber|n
tos|la tos|der Husten|n
resfrio|el resfrío|die Erkältung|n|Anderswo „resfriado“.
gripe|la gripe|die Grippe|n
diarrea|la diarrea|der Durchfall|n
nauseas|las náuseas|die Übelkeit|n
mareado|mareado|schwindlig|adj
herida|la herida|die Wunde|n
quemadura|la quemadura|die Verbrennung|n|quemadura de sol = Sonnenbrand
picadura|la picadura|der Stich (Insekt)|n
alergia|la alergia|die Allergie|n|Soy alérgico a … = Ich bin allergisch gegen …
enfermo|enfermo|krank|adj
cansado|cansado|müde|adj
embarazada|embarazada|schwanger|adj|Achtung: heisst nicht „verlegen“!
"""),
("Arzt & Apotheke", """
medico|el médico|der Arzt|n|Ärztin: la médica.
hospital|el hospital|das Krankenhaus|n
guardia|la guardia|die Notaufnahme|n|ir a la guardia = in die Notaufnahme gehen
farmacia|la farmacia|die Apotheke|n
remedio|el remedio|das Medikament|n|Auch: el medicamento.
pastilla|la pastilla|die Tablette|n
receta|la receta|das Rezept|n
turno|el turno|der Termin|n|Anderswo „cita“.
seguro|el seguro|die Versicherung|n|seguro de viaje = Reiseversicherung
ambulancia|la ambulancia|der Krankenwagen|n|Notruf in Argentinien: 107 (Notarzt), 911 (Polizei).
protector_solar|el protector solar|die Sonnencreme|n
repelente|el repelente|das Mückenschutzmittel|n|Wichtig wegen Dengue.
curita|la curita|das Pflaster|n|Argentinisch.
"""),
("Notfall", """
ayuda|la ayuda|die Hilfe|n
socorro|¡socorro!|Hilfe!|int
emergencia|la emergencia|der Notfall|n
policia|la policía|die Polizei|n
robo|el robo|der Diebstahl, Raub|n
accidente|el accidente|der Unfall|n
perder|perder|verlieren; verpassen|v|yo pierdo, vos perdés; Perdí … = Ich habe … verloren.
robar|robar|stehlen|v|Me robaron … = Man hat mir … gestohlen.
denuncia|la denuncia|die Anzeige|n|hacer una denuncia = Anzeige erstatten
celular|el celular|das Handy|n
billetera|la billetera|die Geldbörse|n
cuidado|¡cuidado!|Vorsicht!|int
peligroso|peligroso|gefährlich|adj
llamar|llamar|anrufen; rufen|v
"""),
("Verben & Wendungen", """
doler|doler|wehtun|v|me duele (Singular), me duelen (Plural)
sentirse|sentirse|sich fühlen|v|Me siento mal. = Ich fühle mich schlecht.
tener_que|tener que|müssen|ph|Tengo que … = Ich muss …
hay_que|hay que|man muss|ph
mal|mal|schlecht|adv
"""),
]
sentences = [
("Me duele la cabeza.","Ich habe Kopfschmerzen.","doler cabeza","doler"),
("Me duele mucho la panza.","Mir tut der Bauch sehr weh.","doler panza mucho","doler"),
("Me duele la garganta.","Ich habe Halsschmerzen.","garganta doler","doler"),
("Me duele el pie derecho.","Mir tut der rechte Fuss weh.","pie doler","doler"),
("Me duele la pierna.","Mir tut das Bein weh.","pierna doler","doler"),
("Tengo dolor de espalda.","Ich habe Rückenschmerzen.","dolor espalda","doler"),
("Tengo dolor de muela.","Ich habe Zahnschmerzen.","dolor muela","doler"),
("Me lastimé la mano.","Ich habe mich an der Hand verletzt.","mano"),
("Me duele el brazo.","Mir tut der Arm weh.","brazo doler","doler"),
("Tengo algo en el ojo.","Ich habe etwas im Auge.","ojo"),
("Me duele el oído.","Ich habe Ohrenschmerzen.","oido doler","doler"),
("Se me rompió un diente.","Mir ist ein Zahn abgebrochen.","diente"),
("Tengo un dolor fuerte en el pecho.","Ich habe starke Schmerzen in der Brust.","dolor pecho"),
("Me caí y me duele la rodilla.","Ich bin gestürzt und mir tut das Knie weh.","rodilla doler","doler"),
("Tengo fiebre.","Ich habe Fieber.","fiebre","tener_hambre"),
("Tengo tos y estoy resfriado.","Ich habe Husten und bin erkältet.","tos","estar_estado"),
("Es solo un resfrío.","Es ist nur eine Erkältung.","resfrio"),
("Creo que tengo gripe.","Ich glaube, ich habe die Grippe.","gripe"),
("Tengo diarrea.","Ich habe Durchfall.","diarrea"),
("Tengo náuseas.","Mir ist übel.","nauseas"),
("Estoy mareado.","Mir ist schwindlig.","mareado","estar_estado"),
("Estoy enfermo.","Ich bin krank.","enfermo","estar_estado"),
("Estoy muy cansado.","Ich bin sehr müde.","cansado muy","estar_estado"),
("Me siento mal.","Ich fühle mich schlecht.","sentirse mal"),
("Mi mujer está embarazada.","Meine Frau ist schwanger.","embarazada","estar_estado"),
("Es una herida pequeña.","Es ist eine kleine Wunde.","herida"),
("Tengo una quemadura de sol.","Ich habe einen Sonnenbrand.","quemadura"),
("Es una picadura de mosquito.","Das ist ein Mückenstich.","picadura"),
("Tengo alergia a la penicilina.","Ich bin gegen Penicillin allergisch.","alergia"),
("Necesito un médico.","Ich brauche einen Arzt.","medico necesitar"),
("¿Dónde está el hospital?","Wo ist das Krankenhaus?","hospital donde"),
("Tenemos que ir a la guardia.","Wir müssen in die Notaufnahme.","guardia tener_que","tener_que"),
("¿Hay una farmacia cerca?","Gibt es eine Apotheke in der Nähe?","farmacia cerca hay"),
("¿Tiene algo para el dolor de cabeza?","Haben Sie etwas gegen Kopfschmerzen?","dolor cabeza"),
("Tomá este remedio dos veces por día.","Nimm dieses Medikament zweimal am Tag.","remedio tomar dos","imperativo"),
("Una pastilla cada ocho horas.","Eine Tablette alle acht Stunden.","pastilla ocho"),
("¿Necesito receta?","Brauche ich ein Rezept?","receta necesitar"),
("Quiero sacar un turno.","Ich möchte einen Termin vereinbaren.","turno querer"),
("¿Tenés seguro de viaje?","Hast du eine Reiseversicherung?","seguro","voseo"),
("¡Llamen a una ambulancia!","Ruft einen Krankenwagen!","ambulancia llamar"),
("Necesito protector solar.","Ich brauche Sonnencreme.","protector_solar necesitar"),
("Hay que usar repelente por los mosquitos.","Wegen der Mücken muss man Mückenschutz benutzen.","repelente hay_que","tener_que"),
("¿Me das una curita?","Gibst du mir ein Pflaster?","curita","voseo"),
("¡Socorro! ¡Ayuda!","Hilfe! Hilfe!","socorro ayuda"),
("Necesito ayuda, por favor.","Ich brauche Hilfe, bitte.","ayuda necesitar por_favor"),
("Es una emergencia.","Es ist ein Notfall.","emergencia"),
("Llamá a la policía.","Ruf die Polizei.","policia llamar","imperativo"),
("Me robaron la billetera.","Man hat mir die Geldbörse gestohlen.","robar billetera"),
("Perdí mi celular.","Ich habe mein Handy verloren.","perder celular"),
("Quiero hacer una denuncia por robo.","Ich möchte einen Diebstahl anzeigen.","denuncia robo"),
("Hubo un accidente en la ruta.","Es gab einen Unfall auf der Landstrasse.","accidente"),
("¡Cuidado con el auto!","Vorsicht, das Auto!","cuidado auto"),
("Ese barrio es peligroso de noche.","Das Viertel ist nachts gefährlich.","peligroso ese noche"),
("Tengo que tomar la pastilla.","Ich muss die Tablette nehmen.","tener_que pastilla tomar","tener_que"),
("Hay que llamar al médico.","Man muss den Arzt rufen.","hay_que llamar medico","tener_que"),
("No me siento bien.","Ich fühle mich nicht gut.","sentirse bien no"),
("Tengo que ir al médico mañana.","Ich muss morgen zum Arzt.","tener_que medico","tener_que"),
]
grammar = [
 {"id":"doler","title":"me duele – es tut mir weh",
  "explanation":"doler funktioniert wie „gefallen“: Das, was wehtut, ist das Subjekt.\n• Me duele la cabeza. – Mir tut der Kopf weh. (Singular: duele)\n• Me duelen los pies. – Mir tun die Füsse weh. (Plural: duelen)\n\nWem es wehtut, zeigt das Pronomen davor:\nme (mir), te (dir), le (ihm/ihr/Ihnen), nos (uns), les (ihnen/euch)\n\nKörperteile mit Artikel, nicht mit „mein“: Me duele la espalda.\nAlternative: Tengo dolor de cabeza.",
  "examples":[{"text":"¿Te duele mucho?","de":"Tut es dir sehr weh?"},{"text":"A mi hijo le duele el oído.","de":"Mein Sohn hat Ohrenschmerzen."}]},
 {"id":"tener_que","title":"tener que, hay que – müssen",
  "explanation":"• tener que + Infinitiv – müssen (persönlich)\n  Tengo que ir al médico. – Ich muss zum Arzt.\n  ¿Tenés que trabajar? – Musst du arbeiten?\n\n• hay que + Infinitiv – man muss (allgemein, ohne Person)\n  Hay que llamar a la policía. – Man muss die Polizei rufen.\n\n• necesitar + Infinitiv – brauchen, müssen\n  Necesito descansar. – Ich muss mich ausruhen.",
  "examples":[{"text":"Tenemos que salir temprano.","de":"Wir müssen früh los."},{"text":"Hay que tener cuidado.","de":"Man muss aufpassen."}]},
 {"id":"estar_estado","title":"estar für Zustände",
  "explanation":"Vorübergehende Zustände und Befinden drückt man mit estar aus:\n• Estoy enfermo. – Ich bin krank.\n• Estoy cansada. – Ich bin müde. (Frau)\n• Está mareado. – Ihm ist schwindlig.\n\nestar: yo estoy, vos estás, usted/él está, nosotros estamos, ustedes están.\nDas Adjektiv richtet sich nach der Person: enfermo / enferma / enfermos.",
  "examples":[{"text":"¿Estás bien?","de":"Geht es dir gut?"},{"text":"Estamos muy cansados.","de":"Wir sind sehr müde."}]},
]
wg = {"doler":["doler"],"dolor":["doler"],"tener_que":["tener_que"],"hay_que":["tener_que"],"enfermo":["estar_estado"],"cansado":["estar_estado"],"mareado":["estar_estado"]}
make('07_salud_emergencias.json','es-AR-07',7,'Gesundheit & Notfall','Salud y emergencias',groups,sentences,grammar,wg)
