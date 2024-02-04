from flask import Flask, request, jsonify
import os
import psycopg2


app = Flask(__name__)

def get_connection():
    conn = psycopg2.connect(
        host="localhost",
        database="projekt",
        user="postgres",
        password="karla",
        port = "5432")
    return conn


@app.route('/getUser/<username>')
def getUser(username):
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute(f"SELECT * FROM igrac where korisnicko_ime = '{username}' or (osoba).email = '{username}';")
    cur.execute(f"WITH sq AS (SELECT * FROM igrac WHERE korisnicko_ime = '{username}' or (osoba).email = '{username}') SELECT json_agg(row_to_json(sq)) FROM sq;")
    user = cur.fetchall()
    cur.close()
    conn.close()
    return user

@app.route('/getAllUsers/<id>')
def getAllUser(id):
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute(f"SELECT * FROM igrac where korisnicko_ime = '{username}' or (osoba).email = '{username}';")
    cur.execute(f"WITH sq AS (SELECT * FROM igrac WHERE NOT id_igrac = '{id}') SELECT json_agg(row_to_json(sq)) FROM sq;")
    users = cur.fetchall()
    cur.close()
    conn.close()
    return users

@app.route('/getAllUsers')
def getAllUser1():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS (SELECT * FROM igrac) SELECT json_agg(row_to_json(sq)) FROM sq;")
    users = cur.fetchall()
    cur.close()
    conn.close()
    return users

@app.route('/setUser', methods=['POST'])
def setUser():
    conn = get_connection()
    data = request.json
    osoba = data.get('osoba')
    ime = osoba.get('ime')
    prezime = osoba.get('prezime')
    email = osoba.get('email')
    kor_ime = data.get('korisnicko_ime')
    lozinka = data.get('lozinka')
    cur = conn.cursor()
    cur.execute(f"INSERT INTO public.igrac(id_igrac, osoba, korisnicko_ime, lozinka) VALUES (default, row('{ime}', '{prezime}', '{email}'), '{kor_ime}', '{lozinka}');")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/createGame/<id>')
def createGame(id):
    conn = get_connection()
    kreator = id
    cur = conn.cursor()
    cur.execute(f"WITH r AS (INSERT INTO public.igra(id_igra, zapoceto, zavrseno, kreator, redoslijed) VALUES (default, default, default, {kreator}, default) RETURNING *) SELECT json_agg(row_to_json(r)) FROM r;")
    result = cur.fetchall()
    conn.commit()
    cur.close()
    conn.close()
    return result

@app.route('/getUsersInGame/<gameID>')
def getUsersInGame(gameID):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS (SELECT igrac.* FROM public.igrac JOIN igrac_igra ON igrac.id_igrac=igrac_igra.id_igrac WHERE igrac_igra.id_igra = {gameID}) SELECT json_agg(row_to_json(sq)) FROM sq;")
    users = cur.fetchall()
    cur.close()
    conn.close()
    return users

@app.route('/removeUserFromGame')
def removeUserFromGame():
    gameID  = request.args.get('id_igra', type=int ,default='')
    userId  = request.args.get('id_igrac',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"DELETE FROM public.igrac_igra WHERE id_igra = {gameID} AND id_igrac = {userId};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/addUserToGame')
def addUserToGame():
    gameID  = request.args.get('id_igra', type=int ,default='')
    userId  = request.args.get('id_igrac',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"INSERT INTO public.igrac_igra (id_igrac_igra, id_igrac, id_igra, bodovi, vlakovi, stanice, zadnji_krug) VALUES (default, {userId}, {gameID}, default, default, default, default);")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/startGame', methods=['POST'])
def startGame():
    conn = get_connection()
    data = request.json
    id_igra = data.get('id_igra')
    niz = data.get('redoslijed')
    cur = conn.cursor()
    cur.execute(f"WITH r AS (UPDATE public.igra SET zapoceto=now(), redoslijed = ARRAY {niz} WHERE id_igra={id_igra} RETURNING *) SELECT json_agg(row_to_json(r)) FROM r;")
    result = cur.fetchall()
    conn.commit()
    cur.close()
    conn.close()
    return result

@app.route('/getAllGames/<id_igrac>')
def getAllGames(id_igrac):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS (SELECT igra.* FROM public.igra JOIN public.igrac_igra ON igra.id_igra = igrac_igra.id_igra WHERE igrac_igra.id_igrac = {id_igrac}) SELECT json_agg(row_to_json(sq)) FROM sq;")
    result = cur.fetchall()
    cur.close()
    conn.close()
    return result

@app.route('/getAllMissions')
def getAllMissions():
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute("SELECT id_misija, grad1.*, grad2.*, bodovi, glavna FROM public.misija join grad as grad1 on misija.grad1 = grad1.id_grad join grad as grad2 on misija.grad2 = grad2.id_grad;")
    cur.execute(f"WITH sq AS ( SELECT id_misija, json_build_object( 'id_grad', grad1.id_grad, 'naziv', grad1.naziv) as \"grad1\",json_build_object('id_grad', grad2.id_grad, 'naziv', grad2.naziv) as \"grad2\", bodovi, glavna FROM  public.misija JOIN public.grad as grad1 ON misija.grad1 = grad1.id_grad JOIN public.grad as grad2 ON misija.grad2 = grad2.id_grad)SELECT json_agg(row_to_json(sq)) FROM sq;")
    result = cur.fetchall()
    cur.close()
    conn.close()
    return result

@app.route('/getMyMissions')
def getMyMissions():
    gameID  = request.args.get('id_igra', type=int ,default='')
    userId  = request.args.get('id_igrac',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS ( SELECT igrac_misija.* FROM igrac_misija JOIN igrac_igra on igrac_misija.igrac_igra=igrac_igra.id_igrac_igra where igrac_igra.id_igra = {gameID} and igrac_igra.id_igrac = {userId})SELECT json_agg(row_to_json(sq)) FROM sq;")
    missions = cur.fetchall()
    cur.close()
    conn.close()
    return missions
    
@app.route('/getUserGameId')
def getUserGameId():
    gameID  = request.args.get('id_igra', type=int ,default='')
    userId  = request.args.get('id_igrac',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"SELECT id_igrac_igra FROM igrac_igra WHERE id_igra ={gameID} AND id_igrac = {userId};")
    missions = cur.fetchall()
    cur.close()
    conn.close()
    return missions

@app.route('/setMission')
def setMission():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    missionId  = request.args.get('id_misija',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"INSERT INTO public.igrac_misija (id_igrac_misija, igrac_igra, misija, zavrsena) VALUES (default, {userGameId}, {missionId}, default);")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/finishMission')
def finishMission():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    missionId  = request.args.get('id_misija',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"UPDATE public.igrac_misija SET zavrsena=true WHERE igrac_misija.igrac_igra={userGameId} AND igrac_misija.misija={missionId};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/unfinishMission')
def unfinishMission():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    missionId  = request.args.get('id_misija',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"UPDATE public.igrac_misija SET zavrsena=false WHERE igrac_misija.igrac_igra={userGameId} AND igrac_misija.misija={missionId};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/getUserGame')
def getUserGame():
    gameID  = request.args.get('id_igrac_igra', type=int ,default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS ( SELECT * FROM igrac_igra WHERE id_igrac_igra ={gameID})SELECT json_agg(row_to_json(sq)) FROM sq;")
    missions = cur.fetchall()
    cur.close()
    conn.close()
    return missions

@app.route('/updatePoints')
def updatePoints():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    points  = request.args.get('bodovi',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"UPDATE public.igrac_igra SET bodovi={points} WHERE id_igrac_igra={userGameId};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/getAllTrains')
def getAllTrains():
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute("SELECT id_misija, grad1.*, grad2.*, bodovi, glavna FROM public.misija join grad as grad1 on misija.grad1 = grad1.id_grad join grad as grad2 on misija.grad2 = grad2.id_grad;")
    cur.execute(f"WITH sq AS ( SELECT id_pruga, put, json_build_object( 'id_grad', grad1.id_grad, 'naziv', grad1.naziv) as \"grad1\",json_build_object('id_grad', grad2.id_grad, 'naziv', grad2.naziv) as \"grad2\", boja FROM  public.pruga JOIN public.grad as grad1 ON pruga.grad1 = grad1.id_grad JOIN public.grad as grad2 ON pruga.grad2 = grad2.id_grad)SELECT json_agg(row_to_json(sq)) FROM sq;")
    result = cur.fetchall()
    cur.close()
    conn.close()
    return result

@app.route('/getMyTrains')
def getMyTrains():
    gameID  = request.args.get('id_igra', type=int ,default='')
    userId  = request.args.get('id_igrac',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS ( SELECT igrac_pruga.* FROM igrac_pruga JOIN igrac_igra on igrac_pruga.igrac_igra=igrac_igra.id_igrac_igra where igrac_igra.id_igra = {gameID} and igrac_igra.id_igrac = {userId})SELECT json_agg(row_to_json(sq)) FROM sq;")
    result = cur.fetchall()
    cur.close()
    conn.close()
    return result

@app.route('/deleteTrain')
def deleteTrain():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    trainID = request.args.get('id_pruga', type=int ,default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"DELETE FROM public.igrac_pruga WHERE igrac_igra={userGameId} AND pruga={trainID};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/getTrain')
def getTrain():
    trainId = request.args.get('id_pruga', type=int ,default='')
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute("SELECT id_misija, grad1.*, grad2.*, bodovi, glavna FROM public.misija join grad as grad1 on misija.grad1 = grad1.id_grad join grad as grad2 on misija.grad2 = grad2.id_grad;")
    cur.execute(f"WITH sq AS ( SELECT id_pruga, put, json_build_object( 'id_grad', grad1.id_grad, 'naziv', grad1.naziv) as \"grad1\",json_build_object('id_grad', grad2.id_grad, 'naziv', grad2.naziv) as \"grad2\", boja FROM  public.pruga JOIN public.grad as grad1 ON pruga.grad1 = grad1.id_grad JOIN public.grad as grad2 ON pruga.grad2 = grad2.id_grad WHERE pruga.id_pruga={trainId})SELECT json_agg(row_to_json(sq)) FROM sq;")
    result = cur.fetchall()
    cur.close()
    conn.close()
    return result

@app.route('/getPlayedTrains')
def getPlayedTrains():
    gameID  = request.args.get('id_igra', type=int ,default='')
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute("SELECT id_misija, grad1.*, grad2.*, bodovi, glavna FROM public.misija join grad as grad1 on misija.grad1 = grad1.id_grad join grad as grad2 on misija.grad2 = grad2.id_grad;")
    cur.execute(f"WITH sq AS ( SELECT id_pruga, put, json_build_object( 'id_grad', grad1.id_grad, 'naziv', grad1.naziv) as \"grad1\",json_build_object('id_grad', grad2.id_grad, 'naziv', grad2.naziv) as \"grad2\", boja FROM  public.pruga JOIN public.grad as grad1 ON pruga.grad1 = grad1.id_grad JOIN public.grad as grad2 ON pruga.grad2 = grad2.id_grad JOIN igrac_pruga on igrac_pruga.pruga = pruga.id_pruga join igrac_igra on igrac_pruga.igrac_igra = igrac_igra.id_igrac_igra where igrac_igra.id_igra = {gameID})SELECT json_agg(row_to_json(sq)) FROM sq;")
    result = cur.fetchall()
    cur.close()
    conn.close()
    return result

@app.route('/setTrain')
def setTrain():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    trainId = request.args.get('id_pruga', type=int ,default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"INSERT INTO public.igrac_pruga (id_igrac_pruga, igrac_igra, pruga) VALUES (default, {userGameId}, {trainId});")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/updateTrainPoints')
def updateTrainPoints():
    userGameId = request.args.get('id_igrac_igra', type=int ,default='')
    points  = request.args.get('bodovi',type=int , default='')
    trainNum = request.args.get('vlakovi',type=int , default='')
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"UPDATE public.igrac_igra SET bodovi={points}, vlakovi={trainNum} WHERE id_igrac_igra={userGameId};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/finishGame/<id>')
def finishGame(id):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"UPDATE public.igra SET zavrseno=now() WHERE id_igra={id};")
    if(cur.rowcount !=1):
        return 'fail' 
    conn.commit()
    cur.close()
    conn.close()
    return 'ok'

@app.route('/getUsersGamesInGame/<gameID>')
def getUsersGamesInGame(gameID):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(f"WITH sq AS (SELECT igrac_igra.* FROM public.igrac_igra WHERE igrac_igra.id_igra = {gameID}) SELECT json_agg(row_to_json(sq)) FROM sq;")
    users = cur.fetchall()
    cur.close()
    conn.close()
    return users

@app.route('/getUserWithID/<id>')
def getUserWithID(id):
    conn = get_connection()
    cur = conn.cursor()
    #cur.execute(f"SELECT * FROM igrac where korisnicko_ime = '{username}' or (osoba).email = '{username}';")
    cur.execute(f"WITH sq AS (SELECT * FROM igrac WHERE id_igrac = {id}) SELECT json_agg(row_to_json(sq)) FROM sq;")
    user = cur.fetchall()
    cur.close()
    conn.close()
    return user

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True)
