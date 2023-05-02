package com.shinkai;

import com.shinkai.generators.UserDataGenerator;
import com.shinkai.models.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.shinkai.lib.RegresInHelpers.getExpTotalPages;
import static com.shinkai.lib.RegresInHelpers.stringToInt;
import static com.shinkai.specs.CreateUserSpec.createUserRequestSpec;
import static com.shinkai.specs.CreateUserSpec.createUserResponseSpec;
import static com.shinkai.specs.DeleteUserSpec.deleteUserRequestSpec;
import static com.shinkai.specs.DeleteUserSpec.deleteUserResponseSpec;
import static com.shinkai.specs.GetListUsersSpec.getUsersRequestSpec;
import static com.shinkai.specs.GetListUsersSpec.getUsersResponseSpec;
import static com.shinkai.specs.UnsuccessfulRegisterUserSpec.noSuccessRegisterRequestSpec;
import static com.shinkai.specs.UnsuccessfulRegisterUserSpec.noSuccessRegisterResponseSpec;
import static com.shinkai.specs.UpdateUserSpec.updateUserRequestSpec;
import static com.shinkai.specs.UpdateUserSpec.updateUserResponseSpec;
import static com.shinkai.specs.UserNotFoundSpec.getUnknownUserRequestSpec;
import static com.shinkai.specs.UserNotFoundSpec.getUnknownUserResponseSpec;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class RegresInApiTests {

    private final UserDataGenerator generator = new UserDataGenerator();

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Description("Check that it is possible to create user with all fields: name, username, email, job and avatar")
    @DisplayName("Successful user creation with all data")
    public void testRegisterNewUser() {
        CreateUserBody userData = new CreateUserBody();
        userData.setUserName(generator.getUsername());
        userData.setEmail(generator.getEmail());
        userData.setName(generator.getFullName());
        userData.setAvatar(generator.getAvatarLink());
        userData.setJob(generator.getJob());

        CreateUserResponse response = step("Make a POST-request to create user", () ->
                given(createUserRequestSpec)
                        .body(userData)
                        .when()
                        .post()
                        .then()
                        .spec(createUserResponseSpec)
                        .extract().as(CreateUserResponse.class));

        step("Checking actual and expected username, name, email, job and avatar", () ->
                assertAll(
                        () -> assertEquals(userData.getName(), response.getName()),
                        () -> assertEquals(userData.getUserName(), response.getUserName()),
                        () -> assertEquals(userData.getJob(), response.getJob()),
                        () -> assertEquals(userData.getEmail(), response.getEmail()),
                        () -> assertEquals(userData.getAvatar(), response.getAvatar())
                )
        );
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Description("Check that it is possible to update user's job")
    @DisplayName("Successful update of user's job")
    public void testUpdateJob() {
        String userId = "3";
        UpdateUserBody updUserJob = new UpdateUserBody();
        updUserJob.setJob(generator.getJob());

        UpdateUserResponse response = step("Make a PUT-request to update user's data", () ->
                given(updateUserRequestSpec)
                        .body(updUserJob)
                        .when()
                        .put("/" + userId)
                        .then()
                        .spec(updateUserResponseSpec)
                        .extract().as(UpdateUserResponse.class));

        step("Verify response of update job request", () ->
                assertThat(response.getJob()).isEqualTo(updUserJob.getJob()));
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Description("Check that it is possible to delete a user")
    @DisplayName("Successful user deletion")
    public void testDeleteUser() {
        String userId = "5";

        step("Make a DELETE user request", () ->
                given(deleteUserRequestSpec)
                        .when()
                        .delete("/" + userId)
                        .then()
                        .spec(deleteUserResponseSpec));
    }


    @ValueSource(strings = {"email", "password"})
    @ParameterizedTest(name = "Unsuccessful user registration: missing parameter {0}")
    @Severity(SeverityLevel.NORMAL)
    @Description("Check that it is not possible to register a user if one of the parameters is missing")
    public void testRegisterWithoutOneParam(String parameter) {
        String email = "lindsay.ferguson@reqres.in";
        RegistrationBody registerData = new RegistrationBody();
        switch (parameter) {
            case "email":
                registerData.setEmail("");
                registerData.setPassword(generator.getPassword());
                break;
            case "password":
                registerData.setPassword("");
                registerData.setEmail(email);
                break;
        }

        ErrorResponse response = step("Make an unsuccessful register user request", () ->
                given(noSuccessRegisterRequestSpec)
                        .body(registerData)
                        .when()
                        .post()
                        .then()
                        .spec(noSuccessRegisterResponseSpec)
                        .extract().as(ErrorResponse.class));

        step("Verify error message", () ->
                assertThat(response.getError()).contains("Missing " + parameter));
    }

    @ParameterizedTest(name = "Get list of users with per_page={0}")
    @ValueSource(strings = {"6", "5", "12", "1"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("Check that it is possible to get users' list with their data")
    public void testGetUsersList(String perPage) {
        int expTotal = 12;
        int expTotalPages = getExpTotalPages(perPage, expTotal);

        GetListOfUsersResponse response = step("Sending request to get users", () ->
                given(getUsersRequestSpec)
                        .when()
                        .get("?per_page=" + perPage)
                        .then()
                        .spec(getUsersResponseSpec)
                        .extract().as(GetListOfUsersResponse.class));

        step("Verify page, per_page, total and total_pages", () -> assertAll(
                () -> assertEquals(1, response.getPage()),
                () -> assertEquals(stringToInt(perPage), response.getPerPage()),
                () -> assertEquals(expTotal, response.getTotal()),
                () -> assertEquals(expTotalPages, response.getTotalPages()))
        );

        step("Verify emails, avatars and number of users", () -> assertAll(
                () -> assertTrue(response.getData().stream().allMatch(x -> x.getEmail().endsWith("reqres.in"))),
                () -> assertTrue(response.getData().stream().allMatch(x -> x.getAvatar().startsWith("https://reqres.in/img/faces/"))),
                () -> MatcherAssert.assertThat(response.getData().size(),
                        allOf(greaterThan(0), lessThanOrEqualTo(stringToInt(perPage))))
        ));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Check data of user with id which doesn't exist")
    @DisplayName("Get data of unknown user")
    public void testGetUnknownUser() {
        String userId = "100";

        step("Get unknown user", () ->
                given(getUnknownUserRequestSpec)
                        .when()
                        .get("/" + userId)
                        .then()
                        .spec(getUnknownUserResponseSpec));
    }
}
