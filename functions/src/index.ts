import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

exports.addAdminCourseId = functions.https.onCall((data, context)=>{
  functions.logger.info("on call has been called", {structuredData: true});
  const email = data.email;
  const departmentId = data.departmentId;
  return setAdminCourseId(email, departmentId).then(()=>{
    return {
      result: `Request fulfilled! ${email} is now an administrator`,
    };
  });
});
/**
     * Processing input parameters
     * @param {string} email
     * @param {string} departmentId
     */
async function setAdminCourseId(email:string, departmentId:string)
  : Promise<void> {
  functions.logger.info(`The email is ${email}`);
  const user = await admin.auth().getUserByEmail(email);
  functions.logger.info(
      `setting ${email} as administrator with course id ${
        departmentId
      }`, {structuredData: true});
  return admin.auth().setCustomUserClaims(user.uid, {
    admin: true,
    departmentId: departmentId,
  });
}

exports.addCourseId = functions.https.onCall((data, context)=>{
  const departmentId = data.departmentId;
  const email = data.email;
  return setCourseId(email, departmentId).then(()=>{
    return {
      result: `Request fulfilled! Group id ${departmentId} set`,
    };
  });
});

/**
     * Processing output parameters
     * @param {string} email
     * @param {string} departmentId
     */
async function setCourseId(email:string, departmentId:string) {
  const user = await admin.auth().getUserByEmail(email);
  functions.logger.info(
      `setting the course id to ${departmentId}`, {structuredData: true});
  return admin.auth().setCustomUserClaims(user.uid, {
    departmentId: departmentId,
  });
}
