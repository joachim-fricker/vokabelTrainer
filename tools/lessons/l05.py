from lib import make
groups = [
("Verkehrsmittel", """
onibus|o ônibus|der Bus|n|Plural ebenfalls: os ônibus
metro|o metrô|die U-Bahn|n
trem|o trem|der Zug|n
aviao|o avião|das Flugzeug|n
taxi|o táxi|das Taxi|n
carro|o carro|das Auto|n
moto|a moto|das Motorrad|n
bicicleta|a bicicleta|das Fahrrad|n
barco|o barco|das Boot|n
balsa|a balsa|die Fähre|n
aplicativo|o aplicativo|die App (z. B. für Fahrdienste)|n|Umgangssprachlich: o app. „Chamar um Uber“ = einen Fahrdienst rufen.
"""),
("Flughafen, Bahnhof & Tickets", """
aeroporto|o aeroporto|der Flughafen|n
rodoviaria|a rodoviária|der Busbahnhof|n|Fernbusse sind in Brasilien das wichtigste Reisemittel über Land.
estacao|a estação|der Bahnhof, die Station|n
ponto_onibus|o ponto de ônibus|die Bushaltestelle|n
passagem|a passagem|die Fahrkarte, das Ticket|n
bilheteria|a bilheteria|der Ticketschalter|n
voo|o voo|der Flug|n
portao|o portão|das Gate; das Tor|n
embarque|o embarque|das Boarding, das Einsteigen|n|desembarque = Ankunft (am Flughafen)
bagagem|a bagagem|das Gepäck|n
mala|a mala|der Koffer|n
mochila|a mochila|der Rucksack|n
horario|o horário|der Fahrplan; die Uhrzeit|n
ida_e_volta|ida e volta|hin und zurück|ph|só ida = nur Hinfahrt
atrasado|atrasado|verspätet|adj
"""),
("Wegbeschreibung", """
rua|a rua|die Strasse|n
avenida|a avenida|die (grosse) Strasse, die Allee|n
praca|a praça|der Platz|n
centro|o centro|das Zentrum|n
mapa|o mapa|die Karte, der Stadtplan|n|Männlich, obwohl auf -a!
esquina|a esquina|die Ecke (Strassenecke)|n
semaforo|o semáforo|die Ampel|n|Umgangssprachlich auch: o sinal, in São Paulo: o farol
direita|à direita|rechts|adv
esquerda|à esquerda|links|adv
reto|reto|geradeaus|adv|Meist: sempre reto
perto|perto|nahe|adv|perto de = in der Nähe von
longe|longe|weit (weg)|adv
aqui|aqui|hier|adv
ali|ali|dort|adv
atras|atrás de|hinter|prep
ao_lado|ao lado de|neben|prep
a_pe|a pé|zu Fuss|ph
"""),
("Mit dem Auto", """
estrada|a estrada|die Landstrasse|n
gasolina|a gasolina|das Benzin|n
posto|o posto de gasolina|die Tankstelle|n
carteira_motorista|a carteira de motorista|der Führerschein|n
pedagio|o pedágio|die Maut, die Mautstelle|n
alugar|alugar|mieten|v
"""),
("Orte in der Stadt", """
museu|o museu|das Museum|n
igreja|a igreja|die Kirche|n
ponte|a ponte|die Brücke|n
"""),
("Verben", """
ir|ir|gehen, fahren|v
vir|vir|kommen|v
pegar|pegar|nehmen (ein Verkehrsmittel)|v|pegar o ônibus = den Bus nehmen
descer|descer|aussteigen; hinuntergehen|v
subir|subir|einsteigen; hinaufgehen|v
esperar|esperar|warten|v
perder|perder|verpassen; verlieren|v
viajar|viajar|reisen|v
voltar|voltar|zurückkommen|v
levar|levar|dauern; mitnehmen|v|Leva meia hora. = Es dauert eine halbe Stunde.
andar_verb|andar|gehen, laufen; fahren (mit)|v|andar de ônibus = Bus fahren
"""),
]
sentences = [
("Onde fica o ponto de ônibus?","Wo ist die Bushaltestelle?","onde ponto_onibus ficar","fragewoerter"),
("Esse ônibus vai para o centro?","Fährt dieser Bus ins Zentrum?","onibus ir centro","ir_futuro"),
("Eu vou pegar o metrô.","Ich nehme die U-Bahn.","eu pegar metro ir","ir_futuro"),
("Uma passagem para o Rio, por favor.","Eine Fahrkarte nach Rio, bitte.","passagem por_favor"),
("Ida e volta, por favor.","Hin und zurück, bitte.","ida_e_volta por_favor"),
("A que horas sai o próximo ônibus?","Wann fährt der nächste Bus?","onibus hora sair"),
("Onde é a bilheteria?","Wo ist der Ticketschalter?","onde bilheteria"),
("Quanto tempo leva até a rodoviária?","Wie lange dauert es bis zum Busbahnhof?","levar rodoviaria"),
("Leva uns vinte minutos de táxi.","Mit dem Taxi dauert es etwa zwanzig Minuten.","levar num_20 minuto taxi"),
("O meu voo está atrasado.","Mein Flug hat Verspätung.","voo atrasado"),
("O embarque é no portão doze.","Das Boarding ist an Gate zwölf.","embarque portao num_12","praep_artikel"),
("Perdi o meu voo!","Ich habe meinen Flug verpasst!","perder voo"),
("A minha mala não chegou.","Mein Koffer ist nicht angekommen.","mala nao chegar"),
("Quantas malas você tem?","Wie viele Koffer hast du?","mala voce ter","fragewoerter"),
("Só tenho uma mochila e uma mala pequena.","Ich habe nur einen Rucksack und einen kleinen Koffer.","mochila mala"),
("A bagagem de mão pode ter dez quilos.","Das Handgepäck darf zehn Kilo haben.","bagagem num_10 ter"),
("Como eu chego ao aeroporto?","Wie komme ich zum Flughafen?","como chegar aeroporto","praep_artikel"),
("Onde fica a estação de metrô?","Wo ist die U-Bahn-Station?","onde estacao metro ficar"),
("O trem para São Paulo sai às oito.","Der Zug nach São Paulo fährt um acht.","trem num_8 sair"),
("Qual é o horário do trem?","Wie ist der Fahrplan des Zuges?","qual horario trem"),
("Siga reto e vire à direita.","Gehen Sie geradeaus und biegen Sie rechts ab.","reto direita","anweisungen"),
("Na esquina, vire à esquerda.","An der Ecke biegen Sie links ab.","esquina esquerda","anweisungen praep_artikel"),
("Depois do semáforo, é a segunda rua.","Nach der Ampel ist es die zweite Strasse.","semaforo rua"),
("O museu fica na avenida principal.","Das Museum liegt an der Hauptstrasse.","museu avenida ficar","praep_artikel"),
("A igreja fica ao lado da praça.","Die Kirche ist neben dem Platz.","igreja ao_lado praca ficar","praep_artikel"),
("O banco fica atrás do museu.","Die Bank ist hinter dem Museum.","atras museu ficar","praep_artikel"),
("É perto ou longe daqui?","Ist es nah oder weit von hier?","perto longe"),
("É bem perto, dá para ir a pé.","Es ist ganz nah, man kann zu Fuss gehen.","perto ir a_pe"),
("O hotel é aqui ou ali?","Ist das Hotel hier oder dort?","hotel aqui ali"),
("Pode me mostrar no mapa?","Können Sie es mir auf der Karte zeigen?","mapa","praep_artikel"),
("Desça no próximo ponto de ônibus.","Steigen Sie an der nächsten Bushaltestelle aus.","descer ponto_onibus","anweisungen praep_artikel"),
("Pegue o ônibus número cinco.","Nehmen Sie den Bus Nummer fünf.","pegar onibus numero num_5","anweisungen"),
("Eu quero alugar um carro.","Ich möchte ein Auto mieten.","eu alugar carro"),
("Preciso da sua carteira de motorista.","Ich brauche Ihren Führerschein.","carteira_motorista","precisar_de"),
("Onde tem um posto de gasolina?","Wo gibt es eine Tankstelle?","onde posto","ter_haben"),
("A gasolina está cara.","Das Benzin ist teuer.","gasolina caro"),
("Essa estrada tem muito pedágio.","Diese Strasse hat viele Mautstellen.","estrada pedagio muito"),
("Vamos de bicicleta até a praia?","Fahren wir mit dem Fahrrad zum Strand?","bicicleta ir","ir_futuro"),
("Eu ando de moto todo dia.","Ich fahre jeden Tag Motorrad.","eu andar_verb moto"),
("A balsa para a ilha sai de hora em hora.","Die Fähre zur Insel fährt stündlich.","balsa hora sair"),
("Vamos fazer um passeio de barco.","Wir machen eine Bootsfahrt.","barco ir","ir_futuro"),
("Vou chamar um táxi pelo aplicativo.","Ich rufe ein Taxi über die App.","taxi aplicativo ir","ir_futuro praep_artikel"),
("O avião vai pousar em dez minutos.","Das Flugzeug landet in zehn Minuten.","aviao num_10 minuto ir","ir_futuro"),
("Espere aqui, por favor.","Warten Sie bitte hier.","esperar aqui por_favor","anweisungen"),
("Eu volto amanhã de manhã.","Ich komme morgen früh zurück.","eu voltar amanha"),
("Você vem com a gente?","Kommst du mit uns?","voce vir com"),
("Nós vamos viajar para o Nordeste.","Wir reisen in den Nordosten.","viajar ir","ir_futuro"),
("Pode subir, o ônibus já vai sair.","Steig ein, der Bus fährt gleich ab.","subir onibus sair ir"),
("Posso levar a mochila no ônibus?","Darf ich den Rucksack in den Bus mitnehmen?","levar mochila onibus","praep_artikel"),
("A ponte fica no centro da cidade.","Die Brücke ist im Stadtzentrum.","ponte centro ficar","praep_artikel"),
("Você sabe chegar na praça?","Weisst du, wie man zum Platz kommt?","voce chegar praca","praep_artikel"),
("O trem está atrasado de novo.","Der Zug hat schon wieder Verspätung.","trem atrasado"),
]
grammar = [
 {"id":"praep_artikel","title":"Präposition + Artikel: no, na, do, da …",
  "explanation":"Die Präpositionen em (in, an, auf), de (von, aus) und a (zu, nach) verschmelzen mit dem Artikel:\n\n• em + o = no, em + a = na  (no hotel, na rua)\n• de + o = do, de + a = da  (perto do hotel, ao lado da praça)\n• a + o = ao, a + a = à  (ao aeroporto, à direita)\n• por + o = pelo, por + a = pela  (pelo aplicativo)\n\nIn der Mehrzahl entsprechend: nos, nas, dos, das, aos, às.\n\nIm Alltag sagt man oft „chegar em“ statt „chegar a“: Cheguei no hotel.",
  "examples":[{"text":"Estou no hotel.","de":"Ich bin im Hotel."},{"text":"O museu fica na praça.","de":"Das Museum ist am Platz."},{"text":"Vou ao centro.","de":"Ich gehe ins Zentrum."},{"text":"perto da estação","de":"in der Nähe des Bahnhofs"}]},
 {"id":"ir_futuro","title":"ir – gehen, und die Zukunft mit ir",
  "explanation":"ir (gehen, fahren) ist unregelmässig:\n• eu vou – ich gehe\n• você/ele/ela vai – du gehst, er/sie geht\n• nós vamos – wir gehen\n• vocês/eles/elas vão – ihr geht, sie gehen\n\nMit ir + Grundform drückt man die Zukunft aus – wie „werden“ im Deutschen. So spricht man in Brasilien fast immer:\nVou pegar o ônibus. – Ich werde den Bus nehmen.\n\nVamos! heisst auch „Los geht's!“ oder „Lass uns …“.\n\nMit welchem Verkehrsmittel: ir de ônibus, de carro, de táxi – aber: ir a pé (zu Fuss).",
  "examples":[{"text":"Eu vou viajar amanhã.","de":"Ich werde morgen verreisen."},{"text":"Vamos de metrô?","de":"Fahren wir mit der U-Bahn?"},{"text":"O ônibus vai sair agora.","de":"Der Bus fährt jetzt gleich ab."}]},
 {"id":"anweisungen","title":"Wegbeschreibungen verstehen",
  "explanation":"Bei Anweisungen hört man die Befehlsform (höflich, für você). Bei Verben auf -ar endet sie auf -e, bei -er/-ir auf -a:\n\n• virar → vire (biegen Sie ab)\n• pegar → pegue (nehmen Sie)\n• esperar → espere (warten Sie)\n• descer → desça (steigen Sie aus)\n• seguir → siga (gehen/fahren Sie weiter)\n\nTypisch: Siga reto, vire à esquerda na segunda rua. – Gehen Sie geradeaus und biegen Sie in die zweite Strasse links ab.",
  "examples":[{"text":"Vire à direita.","de":"Biegen Sie rechts ab."},{"text":"Siga sempre reto.","de":"Immer geradeaus."},{"text":"Desça no ponto final.","de":"Steigen Sie an der Endstation aus."}]},
]
wg = {"ir":["ir_futuro"],"direita":["anweisungen"],"esquerda":["anweisungen"],"reto":["anweisungen"]}
make('05_unterwegs.json','pt-BR-05',5,'Unterwegs','Viajando pela cidade e pelo país',groups,sentences,grammar,wg)
