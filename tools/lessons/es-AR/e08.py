from _common import make
groups = [
("Herkunft & Wohnort", """
ser|ser|sein (Herkunft, Eigenschaft, Beruf)|v|yo soy, vos sos, usted es, nosotros somos
estar|estar|sein (Zustand, Ort)|v|yo estoy, vos estás, usted está
de_donde|¿de dónde?|woher?|adv
alemania|Alemania|Deutschland|n
argentina|Argentina|Argentinien|n
argentino|argentino|argentinisch; Argentinier|adj
pais|el país|das Land|n
ciudad|la ciudad|die Stadt|n
pueblo|el pueblo|das Dorf, die Kleinstadt|n
barrio|el barrio|das Stadtviertel|n
vivir|vivir|wohnen, leben|v
extranjero|extranjero|ausländisch; Ausländer|adj
porteno|porteño|aus Buenos Aires|adj|So nennt man die Einwohner von Buenos Aires.
"""),
("Familie", """
familia|la familia|die Familie|n
padre|el padre|der Vater|n|Umgangssprachlich: papá.
madre|la madre|die Mutter|n|Umgangssprachlich: mamá.
papas|los papás|die Eltern|n|Auch: los padres.
hijo|el hijo|der Sohn|n|los hijos = die Kinder (Söhne und Töchter)
hija|la hija|die Tochter|n
hermano|el hermano|der Bruder|n
hermana|la hermana|die Schwester|n
esposo|el esposo|der Ehemann|n|Ehefrau: la esposa; oft auch mi marido / mi mujer.
novio|el novio|der (feste) Freund|n|la novia = die Freundin
amigo|el amigo|der Freund (Kumpel)|n
abuelo|el abuelo|der Grossvater|n
nieto|el nieto|der Enkel|n
casado|casado|verheiratet|adj
soltero|soltero|ledig|adj
chicos|los chicos|die Kinder; die Jungs|n|In Argentinien auch: los nenes.
"""),
("Beruf & Studium", """
trabajo|el trabajo|die Arbeit|n
laburo|el laburo|die Arbeit (umgangssprachlich)|n|Typisch argentinisch: laburar = arbeiten.
trabajar|trabajar|arbeiten|v
empresa|la empresa|die Firma|n
ingeniero|el ingeniero|der Ingenieur|n
profesor|el profesor|der Lehrer|n|Lehrerin: la profesora.
jubilado|jubilado|in Rente; Rentner|adj
estudiar|estudiar|studieren; lernen|v
estudiante|el estudiante|der Student|n
informatico|el informático|der Informatiker|n
vacaciones|las vacaciones|der Urlaub, die Ferien|n
viaje|el viaje|die Reise|n
"""),
("Smalltalk", """
ano|el año|das Jahr|n|¿Cuántos años tenés? = Wie alt bist du?
nombre|el nombre|der Name|n
gustar|gustar|gefallen|v|Me gusta … / Me gustan …
encantar|encantar|sehr gefallen, lieben|v|Me encanta. = Ich liebe es.
primera_vez|la primera vez|das erste Mal|n
conocer|conocer|kennen; kennenlernen|v|yo conozco, vos conocés
idioma|el idioma|die Sprache|n|Männlich, obwohl auf -a!
ingles|inglés|Englisch|n
che|che|he!, du (Anrede)|int|Typisch argentinisch – daher der Spitzname von „Che“ Guevara.
copado|copado|toll, cool|adj|Argentinisch.
re|re|total, sehr|adv|Umgangssprachlich: re lindo, re bien.
buena_onda|buena onda|nett, sympathisch|ph|Wörtlich „gute Welle“.
contento|contento|froh, zufrieden|adj
simpatico|simpático|sympathisch, freundlich|adj
mi|mi, mis|mein(e)|pron
tu|tu, tus|dein(e)|pron|Ohne Akzent! (tú mit Akzent = du, in Argentinien kaum benutzt)
su|su, sus|sein(e), ihr(e), Ihr(e)|pron
nuestro|nuestro, nuestra|unser(e)|pron
nosotros|nosotros|wir|pron|Frauen unter sich: nosotras.
"""),
]
sentences = [
("¿De dónde sos?","Woher kommst du?","de_donde ser","ser_estar"),
("Soy de Alemania.","Ich komme aus Deutschland.","ser alemania","ser_estar"),
("Soy alemán, de Múnich.","Ich bin Deutscher, aus München.","aleman ser","ser_estar"),
("¿Vos sos argentino?","Bist du Argentinier?","argentino vos ser","ser_estar"),
("Sí, soy porteño.","Ja, ich bin aus Buenos Aires.","porteno si"),
("Argentina es un país enorme.","Argentinien ist ein riesiges Land.","argentina pais"),
("¿En qué ciudad vivís?","In welcher Stadt wohnst du?","ciudad vivir","voseo"),
("Vivo en un pueblo chico.","Ich wohne in einem kleinen Dorf.","pueblo vivir chico"),
("Mi barrio es muy tranquilo.","Mein Viertel ist sehr ruhig.","barrio mi muy","posesivos"),
("¿Sos extranjero?","Bist du Ausländer?","extranjero ser","voseo"),
("Estoy de vacaciones.","Ich bin im Urlaub.","vacaciones estar","ser_estar"),
("Estamos en Buenos Aires por dos semanas.","Wir sind für zwei Wochen in Buenos Aires.","estar dos","ser_estar"),
("Es mi primera vez en Argentina.","Ich bin zum ersten Mal in Argentinien.","primera_vez argentina mi"),
("Este es mi esposo.","Das ist mein Mann.","esposo mi","posesivos"),
("Tengo un hijo y una hija.","Ich habe einen Sohn und eine Tochter.","hijo hija"),
("¿Estás casado?","Bist du verheiratet?","casado estar","ser_estar"),
("No, soy soltero.","Nein, ich bin ledig.","soltero no ser"),
("Mi familia vive en Alemania.","Meine Familie lebt in Deutschland.","familia mi alemania vivir","posesivos"),
("Mi padre es ingeniero.","Mein Vater ist Ingenieur.","padre ingeniero mi","ser_estar"),
("Mi madre está jubilada.","Meine Mutter ist in Rente.","madre jubilado mi"),
("Mis papás viven en el campo.","Meine Eltern wohnen auf dem Land.","papas mi vivir","posesivos"),
("Tengo dos hermanos y una hermana.","Ich habe zwei Brüder und eine Schwester.","hermano hermana dos"),
("Mi hermano vive en Berlín.","Mein Bruder wohnt in Berlin.","hermano mi vivir"),
("Ella es mi novia.","Sie ist meine Freundin.","novio mi"),
("Es un amigo de la familia.","Er ist ein Freund der Familie.","amigo familia"),
("Mi abuelo tiene noventa años.","Mein Grossvater ist neunzig Jahre alt.","abuelo ano mi"),
("Es mi nieto.","Das ist mein Enkel.","nieto mi"),
("Los chicos están en la escuela.","Die Kinder sind in der Schule.","chicos estar","ser_estar"),
("Hace un año que estudio español.","Ich lerne seit einem Jahr Spanisch.","ano estudiar espanol"),
("¿Cómo es tu nombre?","Wie ist dein Name?","nombre tu como","posesivos"),
("¿A qué te dedicás?","Was machst du beruflich?","que","voseo"),
("Trabajo en una empresa de software.","Ich arbeite in einer Softwarefirma.","trabajar empresa"),
("Soy informático.","Ich bin Informatiker.","informatico ser","ser_estar"),
("Mi trabajo es interesante.","Meine Arbeit ist interessant.","trabajo mi","posesivos"),
("Tengo mucho laburo esta semana.","Ich habe diese Woche viel Arbeit.","laburo mucho"),
("Ella es profesora de inglés.","Sie ist Englischlehrerin.","profesor ingles"),
("Estudio español para el viaje.","Ich lerne Spanisch für die Reise.","estudiar espanol viaje"),
("Mi hija es estudiante.","Meine Tochter ist Studentin.","hija estudiante mi"),
("Me gusta mucho Buenos Aires.","Buenos Aires gefällt mir sehr.","gustar mucho","gustar"),
("¿Te gusta el mate?","Magst du Mate?","gustar mate","gustar"),
("Me encanta el asado.","Ich liebe Asado.","encantar asado","gustar"),
("¡Qué viaje copado!","Was für eine tolle Reise!","viaje copado"),
("Che, ¿vamos a tomar algo?","Du, gehen wir was trinken?","che tomar","ir_a"),
("Es re lindo.","Es ist total schön.","re lindo"),
("Tu hermana es muy buena onda.","Deine Schwester ist sehr nett.","tu hermana buena_onda","posesivos"),
("Estoy muy contento.","Ich bin sehr froh.","contento estar","ser_estar"),
("Él es muy simpático.","Er ist sehr sympathisch.","simpatico ser","ser_estar"),
("¿Querés conocer a mi familia?","Willst du meine Familie kennenlernen?","conocer familia mi"),
("El español es un idioma lindo.","Spanisch ist eine schöne Sprache.","idioma espanol"),
("Hablo alemán e inglés.","Ich spreche Deutsch und Englisch.","aleman ingles hablar"),
("Nosotros somos de Alemania.","Wir sind aus Deutschland.","nosotros alemania ser","ser_estar"),
("Nuestra casa está cerca del río.","Unser Haus ist in der Nähe des Flusses.","nuestro estar cerca","posesivos"),
("¿Usted es el padre de Ana?","Sind Sie der Vater von Ana?","usted padre","vos_usted"),
("Su hijo habla muy bien español.","Ihr Sohn spricht sehr gut Spanisch.","su hijo","posesivos"),
("¿Dónde está tu hotel?","Wo ist dein Hotel?","tu hotel estar","ser_estar"),
("Mi esposa y yo estamos de viaje.","Meine Frau und ich sind auf Reisen.","esposo viaje mi"),
]
grammar = [
 {"id":"ser_estar","title":"ser oder estar?",
  "explanation":"Beide heissen „sein“.\n\nser – was jemand oder etwas IST (dauerhaft):\n• Herkunft: Soy de Alemania.\n• Beruf: Soy informático.\n• Eigenschaft: Es simpático.\n• Uhrzeit, Preis: Son las tres. Es caro.\n\nestar – WO und WIE etwas gerade ist:\n• Ort: Estoy en Buenos Aires. ¿Dónde está el hotel?\n• Zustand: Estoy cansado. Está cerrado.\n• Familienstand meist: Estoy casado.\n\nser: soy, sos, es, somos, son\nestar: estoy, estás, está, estamos, están",
  "examples":[{"text":"Soy alemán, pero estoy en Argentina.","de":"Ich bin Deutscher, aber ich bin in Argentinien."},{"text":"La sopa es rica, pero está fría.","de":"Die Suppe ist lecker, aber sie ist kalt."}]},
 {"id":"posesivos","title":"mi, tu, su – Possessivbegleiter",
  "explanation":"• mi / mis – mein(e)\n• tu / tus – dein(e)  (zu vos ebenfalls tu!)\n• su / sus – sein(e), ihr(e), Ihr(e), euer/eure (ustedes)\n• nuestro / nuestra / nuestros / nuestras – unser(e)\n\nmi, tu, su ändern sich nur im Plural: mi hermano – mis hermanos.\nnuestro richtet sich auch nach dem Geschlecht: nuestra casa.\n\nWeil su vieldeutig ist, sagt man oft: la casa de él / de ella / de usted.",
  "examples":[{"text":"Mis papás y mi hermana","de":"Meine Eltern und meine Schwester"},{"text":"¿Es su valija?","de":"Ist das Ihr Koffer?"}]},
 {"id":"gustar","title":"me gusta – mir gefällt",
  "explanation":"gustar funktioniert wie doler: Das, was gefällt, ist das Subjekt.\n• Me gusta el mate. – Ich mag Mate. (Singular)\n• Me gustan las empanadas. – Ich mag Empanadas. (Plural)\n• Me gusta viajar. – Ich reise gern. (mit Verb immer gusta)\n\nPronomen: me, te, le, nos, les.\nVerstärkt: Me gusta mucho. / Me encanta. (ich liebe es)\nVerneint: No me gusta.",
  "examples":[{"text":"¿Te gusta Buenos Aires?","de":"Gefällt dir Buenos Aires?"},{"text":"Nos encantan los alfajores.","de":"Wir lieben Alfajores."}]},
]
wg = {"ser":["ser_estar"],"estar":["ser_estar","estar_estado"],"mi":["posesivos"],"tu":["posesivos"],"su":["posesivos"],"nuestro":["posesivos"],"gustar":["gustar"],"encantar":["gustar"]}
make('08_charla.json','es-AR-08',8,'Smalltalk: Herkunft, Familie, Beruf','Charla: origen, familia, trabajo',groups,sentences,grammar,wg)
